package com.github.RyanHornby.destinyShaders.service;

import com.github.RyanHornby.destinyShaders.model.Color;
import com.github.RyanHornby.destinyShaders.model.ColorSampleRegions;
import com.github.RyanHornby.destinyShaders.model.SampleRegion;
import com.github.RyanHornby.destinyShaders.model.UpdateResponse;
import com.github.RyanHornby.destinyShaders.model.entity.DestinyTempEntity;
import com.github.RyanHornby.destinyShaders.model.entity.ShaderEntity;
import com.github.RyanHornby.destinyShaders.model.entity.VersionEntity;
import com.github.RyanHornby.destinyShaders.model.exception.DestinyManifestException;
import com.github.RyanHornby.destinyShaders.model.exception.HttpException;
import com.github.RyanHornby.destinyShaders.model.exception.NetworkException;
import com.github.RyanHornby.destinyShaders.repository.main.ShaderRepository;
import com.github.RyanHornby.destinyShaders.repository.main.VersionRepository;
import com.github.RyanHornby.destinyShaders.repository.temp.DestinyTempRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ShaderService {

    @Autowired
    private DestinyTempRepository tempRepository;
    @Autowired
    private VersionRepository versionRepository;
    @Autowired
    private ShaderRepository shaderRepository;
    @Autowired
    private HttpClient httpClient;
    @Autowired
    private String tempDbLocation;
    @Autowired
    private String iconsLocation;
    @Autowired
    private String baseUrl;
    @Autowired
    private ColorSampleRegions colorSampleRegions;

    public List<ShaderEntity> findAll() {
        return shaderRepository.findAll();
    }

    public boolean emptyDb() {
        return shaderRepository.count() == 0;
    }

    public UpdateResponse checkForUpdates() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/Platform/Destiny2/Manifest/")).GET().build();
        HttpResponse<InputStream> response;
        String strResponse;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            strResponse = new BufferedReader(new InputStreamReader(response.body())).lines().collect(Collectors.joining());
        } catch (IOException | InterruptedException e) {
            throw new NetworkException();
        }

        if (response.statusCode() != 200) {
            throw new HttpException(response.statusCode());
        }

        JSONObject jsonObject = new JSONObject(strResponse);
        if (jsonObject.getInt("ErrorCode") != 1) {
            throw new DestinyManifestException(jsonObject.getInt("ErrorCode"));
        }

        if (jsonObject.getJSONObject("Response").getString("version").equals(
                versionRepository.findAll().get(0).getVersion())) {
            return new UpdateResponse(false, "", "");
        }

        return new UpdateResponse(true,
                jsonObject.getJSONObject("Response").getJSONObject("mobileWorldContentPaths").getString("en"),
                jsonObject.getJSONObject("Response").getString("version"));
    }

    @Transactional
    public void refreshDb(UpdateResponse updateResponse) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + updateResponse.getPath())).GET().build();
        HttpResponse<InputStream> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException | InterruptedException e) {
            throw new NetworkException();
        }

        if (response.statusCode() != 200) {
            throw new HttpException(response.statusCode());
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.body().readAllBytes());
        ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);

        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(tempDbLocation);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                fileOutputStream.write(zipInputStream.readAllBytes());
                zipEntry = zipInputStream.getNextEntry();
            }
            fileOutputStream.flush();
            byteArrayInputStream.close();
            zipInputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        readTmpDb();
        versionRepository.deleteAll();
        versionRepository.save(new VersionEntity(updateResponse.getVersion()));
    }

    private void readTmpDb() {
        List<DestinyTempEntity> entities = tempRepository.findAllShaders();
        for (DestinyTempEntity entity : entities) {
            Optional<ShaderEntity> optional = shaderRepository.findById(entity.getId());
            if (optional.isEmpty()) {
                JSONObject jsonObject = new JSONObject(entity.getJson());
                jsonObject = jsonObject.getJSONObject("displayProperties");
                if (jsonObject.getBoolean("hasIcon")) {
                    String filePath = saveIcon(entity.getId(), jsonObject.getString("icon"));
                    ShaderEntity shaderEntity = getColors(entity.getId(), jsonObject.getString("name"), filePath);
                    shaderRepository.save(shaderEntity);
                }
            }
        }
    }

    private String saveIcon(int id, String path) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build();
        HttpResponse<InputStream> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException | InterruptedException e) {
            throw new NetworkException();
        }

        if (response.statusCode() != 200) {
            throw new HttpException(response.statusCode());
        }

        String filePath;
        if (id < 0) {
            filePath = iconsLocation + "n" + id*-1 + ".jpeg";
        } else {
            filePath = iconsLocation + id + ".jpeg";
        }

        FileOutputStream fileOutputStream;
        try {
            File iconFile = new File(filePath);
            iconFile.getParentFile().mkdirs();
            fileOutputStream = new FileOutputStream(iconFile);
            fileOutputStream.write(response.body().readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return filePath;
    }

    private ShaderEntity getColors(int id, String name, String path) {
        ShaderEntity rtn = new ShaderEntity();
        rtn.setId(id);
        rtn.setName(name);
        rtn.setImagePath(path);

        BufferedImage image;
        try {
            image = ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        rtn.setInnerCenter(getAvgColor(image, colorSampleRegions.getInnerCenter()));
        rtn.setOuterCenter(getAvgColor(image, colorSampleRegions.getOuterCenter()));
        rtn.setTrimUpper(getAvgColor(image, colorSampleRegions.getTrimUpper()));
        rtn.setTrimLower(getAvgColor(image, colorSampleRegions.getTrimLower()));
        rtn.setLeft(getAvgColor(image, colorSampleRegions.getLeft()));
        rtn.setRight(getAvgColor(image, colorSampleRegions.getRight()));
        rtn.setUp(getAvgColor(image, colorSampleRegions.getUp()));
        rtn.setDown(getAvgColor(image, colorSampleRegions.getDown()));

        return rtn;
    }

    private Color getAvgColor(BufferedImage image, List<SampleRegion> samples) {
        double sinAvg = 0, cosAvg = 0;
        float sAvg = 0, vAvg = 0;
        int numPixels = 0;
        for (SampleRegion sample : samples) {
            if (sample.getHeight() != null && sample.getWidth() != null) {
                for (int i = 0; i < sample.getWidth(); i++) {
                    for (int j = 0; j < sample.getHeight(); j++) {
                        float[] hsv = getHSV(image.getRGB(sample.getX() + i, sample.getY() + j));
                        sinAvg += Math.sin(hsv[0]);
                        cosAvg += Math.cos(hsv[0]);
                        sAvg += hsv[1];
                        vAvg += hsv[2];
                        numPixels++;
                    }
                }
            } else if (sample.getWidth() != null) {
                for (int i = 0; i < sample.getWidth(); i++) {
                    float[] hsv = getHSV(image.getRGB(sample.getX() + i, sample.getY()));
                    sinAvg += Math.sin(hsv[0]);
                    cosAvg += Math.cos(hsv[0]);
                    sAvg += hsv[1];
                    vAvg += hsv[2];
                    numPixels++;
                }
            } else if (sample.getHeight() != null) {
                for (int i = 0; i < sample.getHeight(); i++) {
                    float[] hsv = getHSV(image.getRGB(sample.getX(), sample.getY() + i));
                    sinAvg += Math.sin(hsv[0]);
                    cosAvg += Math.cos(hsv[0]);
                    sAvg += hsv[1];
                    vAvg += hsv[2];
                    numPixels++;
                }
            } else {
                float[] hsv = getHSV(image.getRGB(sample.getX(), sample.getY()));
                sinAvg += Math.sin(hsv[0]);
                cosAvg += Math.cos(hsv[0]);
                sAvg += hsv[1];
                vAvg += hsv[2];
                numPixels++;
            }
        }

        float hAvg = (float) (Math.atan2(sinAvg, cosAvg) * 180 / Math.PI);
        if (hAvg < 0) {
            hAvg += 360;
        }

        return getColor(hAvg, vAvg / numPixels, sAvg  / numPixels);
    }

    private Color getColor(float h, float v, float s) {
        if (v < 0.15) {
            return Color.BLACK;
        } else if ((s < 0.3 && v < 0.3) || (s < 0.2 && v < 0.8)) {
            return Color.GRAY;
        } else if (s < 0.2) {
            return Color.WHITE;
        } else if (h < 35 && h >= 15) {
            return Color.ORANGE;
        } else if (h < 90 && h >= 35) {
            return Color.YELLOW;
        } else if (h < 150 && h >= 90) {
            return Color.GREEN;
        } else if (h < 210 && h >= 150) {
            return Color.CYAN;
        } else if (h < 260 && h >= 210) {
            return Color.BLUE;
        } else if (h < 280 && h >= 260) {
            return Color.PURPLE;
        } else if (h < 330 && h >= 280) {
            return Color.PINK;
        } else if (h >= 330 || h < 15) {
            return Color.RED;
        } else {
            // This shouldn't happen
            throw new RuntimeException("huh?");
        }
    }

    private float[] getHSV(int rgb) {
        int b = rgb & 0xff;
        int g = (rgb & 0xff00) >> 8;
        int r = (rgb & 0xff0000) >> 16;
        float[] rtn = new float[3];
        java.awt.Color.RGBtoHSB(r, g, b, rtn);
        rtn[0] = (float) (rtn[0] * 2 * Math.PI);
        return rtn;
    }
}
