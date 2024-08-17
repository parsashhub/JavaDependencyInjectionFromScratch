package com.example;

import com.example.annotations.Component;
import com.example.annotations.Inject;

@Component
public class MyClient {

    @Inject // or @Autowired
    private MyService myService;

    public void performAction() {
        myService.run();
    }
}
