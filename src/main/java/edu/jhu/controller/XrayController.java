package edu.jhu.controller;


import edu.jhu.controller.data.XrayDto;
import edu.jhu.service.XrayDemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;

/**
 * RestController that handles creating of user data.
 */
@Controller
@Slf4j
public class XrayController {

    private final XrayDemoService demoService;
    public XrayController(XrayDemoService demoService) {
        this.demoService = demoService;
    }

    /**
     * This is the method used by first X-Ray Demo APP 1
     * @param data
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, path = "/")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<XrayDto> handleInitialRequest(@Valid XrayDto data) throws Exception{
        log.info("firstName:{}, lastName:{}", data.getFirstName(), data.getLastName());
        data = demoService.createNewUser(data);

        demoService.sendToSns(data);
        return ResponseEntity.ok(data);
    }

    /**
     * This is the method used by second X-Ray Demo APP 2
     * @param data
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, path = "/create")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<XrayDto> handleSecondaryPostRequest(@Valid XrayDto data) throws Exception{
        log.info("firstName:{}, lastName:{}", data.getFirstName(), data.getLastName());
        return ResponseEntity.ok(demoService.createNewUserDb(data));
    }
}
