package kr.co.metabuild.kotris.util;

import kr.co.metabuild.kotris.config.BeanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CMSUtil {
    private static final Logger logger = LoggerFactory.getLogger(CMSUtil.class);

    private static Environment environment() {
        return BeanContext.get(Environment.class);
    }

    public static String getProperty(String key) {
        return environment().getProperty(key);
    }


    public static Map<String, Object> getMetaProp(String propName) {
        String path = "properties/" + propName + ".properties";
        String propPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(path)).getPath();
//        logger.debug("프로퍼티 경로 ---> {}", propPath);

        final Properties prop = new Properties();

        // 프로퍼티 파일을 읽는다
        try (FileInputStream stream = new FileInputStream(propPath)) {
            prop.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 키값의 Enumeration 객체
        Enumeration<Object> keys = prop.keys();
        // 인터페이스 맵 선언
        Map<String, Object> ifMap = new ConcurrentHashMap<>();
        // 설정파일에 기술된 전체 필드 리스트
        List<Map<String, Object>> fields = new ArrayList<>();

        // IF0300.name=[0300]
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String[] splitKeys = key.split("\\."); // key String 값을 '.'을 기준으로 분리

            String interfaceId = splitKeys[0];
            String propType = splitKeys[1];
            Map<String, Object> fieldItem = new TreeMap<>();

            if ("name".equalsIgnoreCase(propType)) {
                Map<String, Object> info = new ConcurrentHashMap<>();
                info.put("interfaceId", interfaceId);
                info.put("desc", prop.getProperty(key));
                ifMap.put(interfaceId, info);
            } else if ("field".equalsIgnoreCase(propType)) {

                // IF0310.field.name1=1|TCP/IP 송신 바이트수|N|4|4
                Map<String, Object> itemMap = new ConcurrentHashMap<>();
                String[] fieldArr = prop.getProperty(key).split("[|]");

                // 1|TCP/IP 송신 바이트수|N|4|4
                // 2|업무구분코드|S|3|7
                // ...

                String name = splitKeys[2];
                int idx = Integer.parseInt(fieldArr[0]);
                String type = fieldArr[2];
                String nameKr = fieldArr[1];
                int length = Integer.parseInt(fieldArr[3]);
                int offset = Integer.parseInt(fieldArr[4]);

                itemMap.put("interfaceId", interfaceId);
                itemMap.put("name", name);
                itemMap.put("idx", idx);
                itemMap.put("type", type);
                itemMap.put("nameKr", nameKr);
                itemMap.put("length", length);
                itemMap.put("offset", offset);

                fieldItem.put(interfaceId, itemMap);
                fields.add(fieldItem);

            }

        }

        // 전문 필드 정보를 인터페이스 맵에 저장
        ifMap.forEach((ifKey, ifValue) -> {

            List<Map<String, Object>> temp = new ArrayList<>();
            //noinspection unchecked
            Map<String, Object> ifItem = (Map<String, Object>) ifValue;

            fields.forEach(field -> {
                field.keySet().forEach(key -> {
                    if (ifKey.equalsIgnoreCase(key)) {
                        temp.add((Map<String, Object>) field.get(key));
                    }
                });
            });

            // "idx' 필드값 기준으로 오름차순 정렬
            temp.sort(Comparator.comparingInt(o -> (Integer) o.get("idx")));

            ifItem.put("fields", temp);
        });

        return ifMap;
    }

    public static List<Map<String, Object>> getFieldList(Map<String, Object> info, String key) {
        return (List<Map<String, Object>>) ((Map<String, Object>) info.get(key)).get("fields");
    }

    public static Properties getInterfaceProp(String propName) {
        String path = "properties/interface/" + propName + ".properties";
        String propPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(path)).getPath();
        final Properties prop = new Properties();
        // 프로퍼티 파일을 읽는다
        try (FileInputStream stream = new FileInputStream(propPath)) {
            prop.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return prop;
    }

    public static void printProp(String key) {
        String value = CMSUtil.getProperty(key);
        System.out.println(key + ":" + value);
        logger.debug(">> {} = {}", key, value);
    }

    public static void printEnv() {
        logger.debug( "env = {}", environment() );
    }

    public static void printMeta() {

        Map<String, Object> cms = CMSUtil.getMetaProp("cms");
        List<Map<String, Object>> fields = CMSUtil.getFieldList(cms, "cms");
        logger.debug("cms : {}", cms);
        logger.debug("fields : {}", fields);
    }
}
