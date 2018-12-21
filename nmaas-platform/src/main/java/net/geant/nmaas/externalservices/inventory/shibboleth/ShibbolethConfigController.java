package net.geant.nmaas.externalservices.inventory.shibboleth;

import net.geant.nmaas.externalservices.inventory.shibboleth.model.ShibbolethView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/management/shibboleth")
public class ShibbolethConfigController {

    private ShibbolethConfigManager shibboleth;

    @Autowired
    public ShibbolethConfigController(ShibbolethConfigManager shibboleth){
        this.shibboleth = shibboleth;
    }

    @GetMapping
    public ShibbolethView getShibboleth(){
        shibboleth.checkParam();
        return shibboleth.getShibbolethView();
    }

}
