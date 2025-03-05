package kr.co.metabuild.kotris.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MainController {

    @GetMapping(path = {"/", "index", ""})
    public String main() {
        return "Hello World";
    }

    @GetMapping(path = "/map")
    public Map<String, Object> map() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");

        return map;
    }
}
