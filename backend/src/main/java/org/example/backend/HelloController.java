package org.example.backend;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/hello")
    public String hello(){
        return "Hello from Spring Backend!";
    }

}
