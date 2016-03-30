/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.iot.droneanalyzer.controller.api.impl;

import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.controller.api.impl.util.DroneAnalyzerServiceUtils;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.controller.DroneController;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.controller.impl.DroneControllerImpl;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.concurrent.ConcurrentHashMap;

@API( name="drone_analyzer", version="1.0.0", context="/drone_analyzer", tags = {"drone_analyzer"})
@DeviceType( value = "drone_analyzer")
public class DroneControllerService {

    private static org.apache.commons.logging.Log log = LogFactory.getLog(DroneControllerService.class);
    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;
    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();
    private DroneController droneController = new DroneControllerImpl();


    @Path("controller/register/{deviceId}/{ip}/{port}")
    @POST
    public Response registerDeviceIP(@PathParam("deviceId") String deviceId, @PathParam("ip") String deviceIP,
                                   @PathParam("port") String devicePort, @Context HttpServletResponse response) {
        try {
            String result;
            String deviceHttpEndpoint = deviceIP + ":" + devicePort;
            deviceToIpMap.put(deviceId, deviceHttpEndpoint);
            result = "Device-IP Registered";
            response.setStatus(Response.Status.OK.getStatusCode());
            if (log.isDebugEnabled()) {
                log.debug(result);
            }
            return Response.ok(Response.Status.OK.getStatusCode()).build();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Path("controller/device/{deviceId}/send_command")
    @POST
    /*@Feature( code="send_command", name="Send Command", type="operation",
            description="Send Commands to Drone")*/
    public Response droneController(@PathParam("deviceId") String deviceId,
                                    @FormParam("action") String action, @FormParam("duration") String duration,
                                    @FormParam("speed") String speed){
        try {
            DroneAnalyzerServiceUtils.sendControlCommand(droneController, deviceId, action, Double.valueOf(speed),
                    Double.valueOf(duration));
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (DeviceManagementException e) {
            log.error("Drone command didn't success. Try again, \n"+ e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}