package com.vladt.kitesurfingapp.Network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

//for checking if there is an active internet connection
public class InternetConnection {

    public static Boolean check() {
        InetAddress inetAddress = null;
        try {
            Future<InetAddress> future = Executors.newSingleThreadExecutor().submit(new Callable<InetAddress>() {
                @Override
                public InetAddress call() {
                    try {
                        //get host address name
                        return InetAddress.getByName(APIEndpoints.host);
                    } catch (UnknownHostException e) {
                        return null;
                    }
                }
            });

            //set a timeout
            inetAddress = future.get(10000, TimeUnit.MILLISECONDS);
            future.cancel(true);
        } catch (InterruptedException ignored) {
        } catch (ExecutionException ignored) {
        } catch (TimeoutException ignored) {
        }

        //if the address isn't empty and isn't unreachable
        //return valid connection
        return inetAddress != null && !inetAddress.equals("");
    }
}
