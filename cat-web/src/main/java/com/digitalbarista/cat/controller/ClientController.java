package com.digitalbarista.cat.controller;

import com.digitalbarista.cat.business.Client;
import com.digitalbarista.cat.business.ServiceMetadata;
import com.digitalbarista.cat.business.ServiceResponse;
import com.digitalbarista.cat.ejb.session.ClientManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * User: Kris
 * Date: 3/15/13
 * Time: 12:53 PM
 */
@Controller
public class ClientController
{
    @Autowired
    private ClientManager clientManager;

    @RequestMapping(value = "/data/client")
    public ServiceResponse listClients(@RequestParam(required = false) Integer offset,
                                       @RequestParam(required = false) Integer limit,
                                       @RequestParam(required = false) String sort,
                                       @RequestParam(required = false) Integer direction)
    {
        ServiceMetadata meta = new ServiceMetadata(limit, offset, sort, direction);
        ServiceResponse ret = clientManager.getClients(meta);
        return ret;
    }
}
