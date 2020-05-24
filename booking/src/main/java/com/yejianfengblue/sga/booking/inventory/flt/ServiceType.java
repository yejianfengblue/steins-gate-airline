package com.yejianfengblue.sga.booking.inventory.flt;

public enum ServiceType {

    PAX,  // passenger
    FRTR;  // freighter

    boolean isPassenger(ServiceType serviceType) {
        return PAX.equals(serviceType);
    }

    boolean isFreighter(ServiceType serviceType) {
        return FRTR.equals(serviceType);
    }
}
