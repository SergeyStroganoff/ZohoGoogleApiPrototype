package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaHandler implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object input, Context context) {
        try {
            ServiceFactory factory = new ServiceFactory();
            AppLogic2 appLogic = new AppLogic2(factory);
            return appLogic.runSync();
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return "Failed: " + e.getMessage();
        }
    }
}
