package net.geant.nmaas.monitor;

import net.geant.nmaas.monitor.model.MonitorEntryDto;

public enum ServiceType {
    GITLAB {
        @Override
        public String getName() {
            return "GITLAB";
        }

        @Override
        public MonitorEntryDto getDefaultMonitorEntry() {
            return new MonitorEntryDto(this, 1L, TimeFormat.H);
        }
    },
    HELM {
        @Override
        public String getName() {
            return "HELM";
        }

        @Override
        public MonitorEntryDto getDefaultMonitorEntry() {
            return new MonitorEntryDto(this, 1L, TimeFormat.H);
        }
    },
    DATABASE {
        @Override
        public String getName() {
            return "DATABASE";
        }

        @Override
        public MonitorEntryDto getDefaultMonitorEntry() {
            return new MonitorEntryDto(this, 5L, TimeFormat.MIN);
        }
    };

    public abstract String getName();

    public abstract MonitorEntryDto getDefaultMonitorEntry();
}
