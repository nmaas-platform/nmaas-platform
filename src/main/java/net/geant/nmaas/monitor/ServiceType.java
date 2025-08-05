package net.geant.nmaas.monitor;

import net.geant.nmaas.monitor.model.MonitorEntryView;

public enum ServiceType {
    GITLAB {
        @Override
        public String getName() {
            return "GITLAB";
        }

        @Override
        public MonitorEntryView getDefaultMonitorEntry() {
            return new MonitorEntryView(this, 1L, TimeFormat.H);
        }
    },
    HELM {
        @Override
        public String getName() {
            return "HELM";
        }

        @Override
        public MonitorEntryView getDefaultMonitorEntry() {
            return new MonitorEntryView(this, 1L, TimeFormat.H);
        }
    },
    DATABASE {
        @Override
        public String getName() {
            return "DATABASE";
        }

        @Override
        public MonitorEntryView getDefaultMonitorEntry() {
            return new MonitorEntryView(this, 5L, TimeFormat.MIN);
        }
    };

    public abstract String getName();

    public abstract MonitorEntryView getDefaultMonitorEntry();
}
