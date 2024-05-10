var stat_sample = null;

function segment_style(properties, zoom) {
    let opacity = 0.8;
    let color = "#666";
    if (properties.station) {
        if (stat_sample) {
            let sample = stat_sample.samples[properties.station];
            if (sample) {
                let flow = sample[0];
                let speed = sample[1];
                let density = (flow && speed) ? Math.round(flow / speed) : null;
                opacity = 1;
                color = density_color(density);
            }
        }
    }
    return {
        fill: true,
        fillOpacity: opacity,
        fillColor: color,
        stroke: false,
    };
}

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

function dms_style(properties, zoom) {
    // FIXME: use item_style provided by bulb code
    return {
        fill: true,
        fillOpacity: 0.8,
        fillColor: "#fcf",
        weight: 0.5,
        opacity: 0.5,
        color: "#000",
    };
}

function make_styles() {
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
        fillOpacity: 0.8,
        fillColor: "#b5d0d0",
        stroke: false,
    };
    let wetland = {
        fill: true,
        fillOpacity: 0.8,
        fillColor: "#b8d0bd",
        stroke: false,
    };
    let leisure = {
        fill: true,
        fillOpacity: 0.6,
        fillColor: "#88cc88",
        weight: 0.1,
        color: '#000',
        opacity: 0.6,
    };
    let cemetery = {
        fill: true,
        fillOpacity: 0.6,
        fillColor: "#aaccaa",
        weight: 0.1,
        color: '#000',
        opacity: 0.6,
    };
    let building = {
        fill: true,
        fillOpacity: 0.7,
        fillColor: "#bca9a9",
        weight: 0.7,
        color: "#baa",
    };
    let retail = {
        fill: true,
        fillOpacity: 0.25,
        fillColor: "#b99",
        stroke: false,
    };
    let parking = {
        fill: true,
        fillOpacity: 0.6,
        fillColor: "#cca",
        stroke: false,
    };
    let path = {
        color: '#000',
        opacity: 0.5,
        weight: 1,
        dashArray: "1 3",
    };
    let railway = {
        color: '#642',
        opacity: 0.6,
        weight: 2.5,
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
        motorway: { color: "#ffd9a9", weight: 3 },
        trunk: { color: "#ffe0a9" },
        primary: { color: "#ffeaa9" },
        secondary: { color: "#fff4a9" },
        tertiary: { color: "#ffffa9" },
        road: { color: "#eee", weight: 2 },
        path: path,
        railway: railway,
        building: building,
        parking: parking,
        segment_9: segment_style,
        segment_10: segment_style,
        segment_11: segment_style,
        segment_12: segment_style,
        segment_13: segment_style,
        segment_14: segment_style,
        segment_15: segment_style,
        segment_16: segment_style,
        segment_17: segment_style,
        segment_18: segment_style,
        dms_12: dms_style,
        dms_13: dms_style,
        dms_14: dms_style,
        dms_15: dms_style,
        dms_16: dms_style,
        dms_17: dms_style,
        dms_18: dms_style,
    };
}

function init_map() {
    var map = L.map('mapid', {
        center: [45, -93],
        zoom: 12,
    });
    const osm_url = "/tile/{z}/{x}/{y}.mvt";
    const tms_url = "/tms/{z}/{x}/{y}.mvt";
    const highlight_style = {
        fill: true,
        fillColor: 'red',
        fillOpacity: 0.1,
        radius: 6,
        color: 'red',
        opacity: 0.1,
    };
    let options = {
        renderFactory: L.svg.tile,
        interactive: true,
        vectorTileLayerStyles: make_styles(),
        getFeatureId: layer_id,
        attribution: 'Map data © <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>',
        maxNativeZoom: 18,
    };
    var highlight;
    var osm_layers = L.vectorGrid.protobuf(osm_url, options);
    var tms_layers = L.vectorGrid.protobuf(tms_url, options);
    function on_click(e) {
        let fid = layer_id(e.layer);
        let change = (typeof fid != "undefined") && (fid != highlight);
        if (highlight) {
            osm_layers.resetFeatureStyle(highlight);
            tms_layers.resetFeatureStyle(highlight);
            highlight = null;
        }
        if (change) {
            highlight = fid;
            osm_layers.setFeatureStyle(highlight, highlight_style);
            tms_layers.setFeatureStyle(highlight, highlight_style);
            let label = layer_label(e.layer);
            if (label) {
                L.popup({ closeButton: false })
                 .setContent(label)
                 .setLatLng(e.latlng)
                 .openOn(map);
            };
        } else {
            map.closePopup();
        }
        L.DomEvent.stop(e);
    }
    osm_layers.on('click', on_click);
    tms_layers.on('click', on_click);
    osm_layers.addTo(map);
    tms_layers.addTo(map);
}

function layer_id(layer) {
    return layer.properties.osm_id ||
        layer.properties.sid ||
        layer.properties.name;
}

function layer_label(layer) {
    let label = null;
    let name = layer.properties.name || layer.properties.ref;
    if (name) {
        label = name;
        let station = layer.properties.station;
        if (station) {
            label = "<b>" + station + "</b> " + label;
            if (stat_sample) {
                let sample = stat_sample.samples[station];
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
        }
    }
    return label;
}

window.onload = init_map;

fetch('/iris/station_sample')
.then(response => response.json())
.then(result => {
    stat_sample = result;
    console.log('station_sample: ', stat_sample.time_stamp);
})
.catch(error => {
    console.error('Error fetching station_sample: ', error);
});
