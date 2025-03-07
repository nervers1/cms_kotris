package kr.co.metabuild.kotris.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MainController {

    @Autowired
    Environment env;

    public MainController(Environment env) {
        this.env = env;
    }

    @GetMapping(path = {"/", "index", ""})
    public String main() {
        return env.getProperty("IFCOMM.name");
    }

    @GetMapping(path = "/map")
    public Map<String, Object> map() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");


        return map;
    }
}
