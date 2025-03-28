// The Leaflet map
var map;

// Current displayed tooltip
var tooltip = null;

// TMS layers
var tms_layers = null;

// Current TMS selection
var tms_select = null;

// Fly map enabled
var fly_enabled = true;

// Current station sample data
var stat_sample = null;

// Current TMS main item states
var item_states = null;

// Get styles for OSM layers
function osm_styles() {
    let county = {
        fill: true,
        fillColor: "#bbb",
        fillOpacity: 1,
        weight: 0.1,
        color: '#000',
        dashArray: "1 2",
    };
    let city = {
        fill: true,
        fillColor: "#bbb",
        fillOpacity: 1,
        weight: 0.15,
        color: '#000',
    };
    let water = {
        fill: true,
        fillColor: "#b5d0d0",
        fillOpacity: 0.8,
        stroke: false,
    };
    let wetland = {
        fill: true,
        fillColor: "#b8d0bd",
        fillOpacity: 0.8,
        stroke: false,
    };
    let leisure = {
        fill: true,
        fillColor: "#88cc88",
        fillOpacity: 0.6,
        weight: 0.1,
        color: '#000',
        opacity: 0.6,
    };
    let cemetery = {
        fill: true,
        fillColor: "#aaccaa",
        fillOpacity: 0.6,
        weight: 0.1,
        color: '#000',
        opacity: 0.6,
    };
    let building = {
        fill: true,
        fillColor: "#bca9a9",
        fillOpacity: 0.7,
        weight: 0.7,
        color: "#baa",
    };
    let retail = {
        fill: true,
        fillColor: "#b99",
        fillOpacity: 0.25,
        stroke: false,
    };
    let parking = {
        fill: true,
        fillColor: "#cca",
        fillOpacity: 0.6,
        stroke: false,
    };
    let path = {
        weight: 1,
        color: '#000',
        opacity: 0.5,
        dashArray: "1 3",
    };
    let railway = {
        weight: 2.5,
        color: '#642',
        opacity: 0.6,
        lineCap: "butt",
        dashArray: "1 1.5",
    };
    return {
        county: county,
        city: city,
        lake: water,
        river: water,
        water: water,
        pond: water,
        wetland: wetland,
        leisure: leisure,
        cemetery: cemetery,
        retail: retail,
        motorway: { weight: 3, color: "#ffd9a9" },
        trunk: { color: "#ffe0a9" },
        primary: { color: "#ffeaa9" },
        secondary: { color: "#fff4a9" },
        tertiary: { color: "#ffffa9" },
        road: { weight: 2, color: "#eee" },
        path: path,
        railway: railway,
        building: building,
        parking: parking,
    };
}

// Get styles for TMS layers
function tms_styles() {
    return {
        segment_9: tms_style,
        segment_10: tms_style,
        segment_11: tms_style,
        segment_12: tms_style,
        segment_13: tms_style,
        segment_14: tms_style,
        segment_15: tms_style,
        segment_16: tms_style,
        segment_17: tms_style,
        segment_18: tms_style,
        beacon_10: tms_style,
        beacon_11: tms_style,
        beacon_12: tms_style,
        beacon_13: tms_style,
        beacon_14: tms_style,
        beacon_15: tms_style,
        beacon_16: tms_style,
        beacon_17: tms_style,
        beacon_18: tms_style,
        camera_10: tms_style,
        camera_11: tms_style,
        camera_12: tms_style,
        camera_13: tms_style,
        camera_14: tms_style,
        camera_15: tms_style,
        camera_16: tms_style,
        camera_17: tms_style,
        camera_18: tms_style,
        dms_11: tms_style,
        dms_12: tms_style,
        dms_13: tms_style,
        dms_14: tms_style,
        dms_15: tms_style,
        dms_16: tms_style,
        dms_17: tms_style,
        dms_18: tms_style,
        lcs_12: tms_style,
        lcs_13: tms_style,
        lcs_14: tms_style,
        lcs_15: tms_style,
        lcs_16: tms_style,
        lcs_17: tms_style,
        lcs_18: tms_style,
        ramp_meter_11: tms_style,
        ramp_meter_12: tms_style,
        ramp_meter_13: tms_style,
        ramp_meter_14: tms_style,
        ramp_meter_15: tms_style,
        ramp_meter_16: tms_style,
        ramp_meter_17: tms_style,
        ramp_meter_18: tms_style,
        weather_sensor_10: tms_style,
        weather_sensor_11: tms_style,
        weather_sensor_12: tms_style,
        weather_sensor_13: tms_style,
        weather_sensor_14: tms_style,
        weather_sensor_15: tms_style,
        weather_sensor_16: tms_style,
        weather_sensor_17: tms_style,
        weather_sensor_18: tms_style,
    };
}

// Get base TMS style
function tms_style_base() {
    return {
        fill: true,
        fillColor: "#666",
        fillOpacity: 0.8,
        stroke: true,
        weight: 0.5,
        color: "#000",
        opacity: 0.5,
    };
}

// Get TMS style
function tms_style(properties) {
    return tms_style_feature(properties.name, properties.station_id);
}

// Get style for a TMS feature
function tms_style_feature(name, sid) {
    return (name) ? tms_style_item(name) : tms_style_station(sid);
}

// Get style for a TMS item
function tms_style_item(name) {
    let state = '';
    if (item_states) {
        state = item_states[name];
    }
    return item_style(state);
}

// Get style based on main item state
function item_style(state) {
    let style = tms_style_base();
    switch (state) {
        case '🔹':
            style.fillColor = "#55acee";
            return style;
        case '🔶':
            style.fillColor = "#e78e0b";
            return style;
        case '🗓️':
        case '👽':
            style.fillColor = "#ffca81";
            style.fillOpacity = 0.5;
            return style;
        case '⚠️':
            style.fillColor = "black";
            style.fillOpacity = 0.8;
            return style;
        case '🔌':
            return style;
        default:
            style.fill = false;
            style.stroke = false;
            return style;
    }
}

// Get style for a TMS station
function tms_style_station(sid) {
    let style = tms_style_base();
    if (sid && stat_sample) {
        let sample = stat_sample.samples[sid];
        if (sample) {
            let flow = sample[0];
            let speed = sample[1];
            let density = (flow && speed) ? Math.round(flow / speed) : null;
            style.fillColor = density_color(density);
            style.fillOpacity = 1;
        }
    }
    return style;
}

// Get color based on density (veh/mi)
function density_color(density) {
    if (density) {
        if (density < 30) {
            return "#292";
        }
        if (density < 50) {
            return "#fc0";
        }
        if (density < 200) {
            return "#d00";
        }
        return "#c0f";
    }
    return "#666";
}

// Select feature on TMS layers
function select_tms_feature(fid, name, sid) {
    let change = (typeof fid != "undefined") && (fid != tms_select);
    if (tooltip) {
        tooltip.close();
        tooltip = null;
    }
    if (tms_select) {
        tms_layers.resetFeatureStyle(tms_select);
        tms_select = null;
    }
    if (change) {
        let style = tms_style_feature(name, sid);
        style.weight = 2;
        style.color = 'white';
        style.opacity = 1,
        tms_layers.setFeatureStyle(fid, style);
        tms_select = fid;
    }
    return change;
}

// Initialize leaflet map
function init_map() {
    map = L.map('mapid', {
        center: [45, -93],
        zoom: 12,
    });
    map.attributionControl.setPrefix("");
    const osm_url = "/tile/{z}/{x}/{y}.mvt";
    const tms_url = "/tms/{z}/{x}/{y}.mvt";
    let osm_layers = L.vectorGrid.protobuf(osm_url, {
        renderFactory: L.svg.tile,
        interactive: true,
        vectorTileLayerStyles: osm_styles(),
        getFeatureId: osm_layer_id,
        attribution: 'Map data © <a href="https://www.openstreetmap.org/">OpenStreetMap</a>',
        maxNativeZoom: 18,
    });
    tms_layers = L.vectorGrid.protobuf(tms_url, {
        renderFactory: L.svg.tile,
        interactive: true,
        vectorTileLayerStyles: tms_styles(),
        getFeatureId: tms_layer_id,
        attribution: '',
        maxNativeZoom: 18,
    });
    var osm_select;
    function osm_on_click(e) {
        if (tooltip) {
            tooltip.close();
            tooltip = null;
        }
        let fid = osm_layer_id(e.propagatedFrom);
        let change = (typeof fid != "undefined") && (fid != osm_select);
        if (osm_select) {
            osm_layers.resetFeatureStyle(osm_select);
            osm_select = null;
        }
        if (change) {
            osm_select = fid;
            osm_layers.setFeatureStyle(osm_select, {
                fill: true,
                fillColor: 'red',
                fillOpacity: 0.1,
                color: 'red',
                opacity: 0.1,
                radius: 6,
            });
            let label = osm_layer_label(e.propagatedFrom);
            if (label) {
                tooltip = L.tooltip()
                           .setContent(label)
                           .setLatLng(e.latlng)
                           .openOn(map);
            };
        }
        L.DomEvent.stop(e);
    }
    osm_layers.on('click', osm_on_click);
    function tms_on_click(e) {
        let fid = tms_layer_id(e.propagatedFrom);
        let name = e.propagatedFrom.properties.name;
        let sid = e.propagatedFrom.properties.station_id;
        let change = select_tms_feature(fid, name, sid);
        if (change) {
            let label = tms_layer_label(e.propagatedFrom);
            if (label) {
                tooltip = L.tooltip()
                           .setContent(label)
                           .setLatLng(e.latlng)
                           .openOn(map);
            };
            const ev = new CustomEvent("tmsevent", {
                detail: fid,
                bubbles: true,
                cancelable: true,
                composed: false,
            });
            document.querySelector('#mapid').dispatchEvent(ev);
            L.DomEvent.stop(e);
        }
    }
    tms_layers.on('click', tms_on_click);
    osm_layers.addTo(map);
    tms_layers.addTo(map);
    map.on('zoomstart', function () {
        if (tooltip) {
            tooltip.close();
            tooltip = null;
        }
    });
}

// Get OSM layer feature ID
function osm_layer_id(layer) {
    return layer.properties.osm_id;
}

// Get OSM layer feature label
function osm_layer_label(layer) {
    return layer.properties.name || layer.properties.ref;
}

// Get TMS layer feature ID
function tms_layer_id(layer) {
    return layer.properties.tms_id || layer.properties.name;
}

// Get TMS layer feature label
function tms_layer_label(layer) {
    let station_id = layer.properties.station_id;
    if (station_id) {
        let label = "<b>" + station_id + "</b>";
        if (stat_sample) {
            let sample = stat_sample.samples[station_id];
            if (sample) {
                let flow = sample[0];
                if (flow)
                    label += "<br>&nbsp;<b>" + flow + "</b> veh/h";
                let speed = sample[1];
                if (speed)
                    label += "<br>&nbsp;<b>" + speed + "</b> mi/h";
                let density = (flow && speed)
                            ? Math.round(flow / speed)
                            : null;
                if (density)
                    label += "<br>&nbsp;<b>" + density + "</b> veh/lane·mi";
            }
        }
        return label;
    } else {
        return layer.properties.name;
    }
}

window.onload = init_map;
