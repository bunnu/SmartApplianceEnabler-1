/*
 * Copyright (C) 2020 Axel Müller <axel.mueller@avanux.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.MockSwitch;
import de.avanux.smartapplianceenabler.control.StartingCurrentSwitch;
import de.avanux.smartapplianceenabler.control.ev.EVChargerControl;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicle;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import de.avanux.smartapplianceenabler.control.ev.SocScript;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.schedule.*;
import de.avanux.smartapplianceenabler.semp.webservice.*;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class ApplianceBuilder {

    private Appliance appliance;
    boolean initialized = false;

    public ApplianceBuilder(String applianceId) {
        appliance = new Appliance();
        appliance.setId(applianceId);
    }

    public ApplianceBuilder withMockSwitch(boolean asStartingCurrentSwitch) {
        Control control = new MockSwitch();
        if(asStartingCurrentSwitch) {
            StartingCurrentSwitch startingCurrentSwitch = new StartingCurrentSwitch();
            startingCurrentSwitch.setControl(control);
            control = startingCurrentSwitch;
        }

        appliance.setControl(control);
        return this;
    }

    public ApplianceBuilder withEvCharger(EVChargerControl evChargerControl) {
        ElectricVehicleCharger evCharger = new ElectricVehicleCharger();
        return withEvCharger(evCharger, evChargerControl);
    }

    public ApplianceBuilder withEvCharger(ElectricVehicleCharger evCharger, EVChargerControl evChargerControl) {
        evCharger.setStartChargingStateDetectionDelay(0);
        evCharger.setControl(evChargerControl);
        appliance.setControl(evCharger);
        return this;
    }

    public ApplianceBuilder withElectricVehicle(Integer evId, Integer batteryCapacity) {
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setId(evId);
        vehicle.setBatteryCapacity(batteryCapacity);
        addVehicle(vehicle);
        return this;
    }

    public ApplianceBuilder withElectricVehicle(Integer evId, Integer batteryCapacity, Integer defaultSocOptionalEnergy, SocScript socScript) {
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setId(evId);
        vehicle.setBatteryCapacity(batteryCapacity);
        vehicle.setDefaultSocOptionalEnergy(defaultSocOptionalEnergy);
        vehicle.setSocScript(socScript);
        addVehicle(vehicle);
        return this;
    }

    private void addVehicle(ElectricVehicle vehicle) {
        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
        List<ElectricVehicle> vehicles = evCharger.getVehicles();
        if(vehicles == null) {
            vehicles = new ArrayList<>();
            evCharger.setVehicles(vehicles);
        }
        vehicles.add(vehicle);
    }

    public ApplianceBuilder withMockMeter() {
        return this.withMeter(Mockito.mock(Meter.class));
    }

    public ApplianceBuilder withMeter(Meter meter) {
        appliance.setMeter(meter);
        return this;
    }

    public ApplianceBuilder withSchedule(int startHour, int startMinute, int endHour, int endMinute,
                                         Integer minRunningTime, int maxRunningTime) {
//        addSchedule(new Schedule(minRunningTime, maxRunningTime, new TimeOfDay(startHour, startMinute, 0),
//                new TimeOfDay(endHour, endMinute, 0)));
        return this;
    }

    public ApplianceBuilder withRuntimeRequest(LocalDateTime now, LocalDateTime intervalStart, LocalDateTime intervalEnd,
                                               Integer min, Integer max, boolean enabled) {
        Interval interval = new Interval(intervalStart.toDateTime(), intervalEnd.toDateTime());
        RuntimeRequest request = new RuntimeRequest(min, max);
        request.setEnabled(enabled);
        TimeframeInterval timeframeInterval = new TimeframeInterval(interval, request);
        getTimeframeIntervalHandler().addTimeframeInterval(now, timeframeInterval, false);
        return this;
    }

    public ApplianceBuilder withSocRequest(LocalDateTime now, Interval interval,
                                           Integer evId, Integer soc, boolean enabled) {
        SocRequest request = new SocRequest(soc, evId);
        request.setEnabled(enabled);
        TimeframeInterval timeframeInterval = new TimeframeInterval(interval, request);
        getTimeframeIntervalHandler().addTimeframeInterval(now, timeframeInterval, false);
        return this;
    }

    public static void init(List<Appliance> applianceList) {
        Appliances appliances = new Appliances();
        appliances.setAppliances(applianceList);
        ApplianceManager.getInstanceWithoutTimer().setAppliances(appliances);

        Device2EM device2EM = new SempBuilder(appliances).build();
        ApplianceManager.getInstanceWithoutTimer().setDevice2EM(device2EM);

        ApplianceManager.getInstanceWithoutTimer().init();
    }

    public ApplianceBuilder init() {
        ApplianceBuilder.init(Collections.singletonList(appliance));
        initialized = true;
        return this;
    }

    public Appliance build(boolean init) {
        if(init) {
            ApplianceBuilder.init(Collections.singletonList(appliance));
        }
        return appliance;
    }

    private TimeframeIntervalHandler getTimeframeIntervalHandler() {
        if(! initialized) {
            init();
        }
        return appliance.getTimeframeIntervalHandler();
    }

}
