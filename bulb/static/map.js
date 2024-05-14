var stat_sample = null;

function segment_style(properties, zoom) {
    let opacity = 0.8;
    let color = "#666";
    if (properties.station_id) {
        if (stat_sample) {
            let sample = stat_sample.samples[properties.station_id];
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
        fillColor: "#4aa",
        weight: 0.5,
        opacity: 0.5,
        color: "#000",
    };
}

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
    };
}

function tms_styles() {
    return {
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
    map.attributionControl.setPrefix("");
    const osm_url = "/tile/{z}/{x}/{y}.mvt";
    const tms_url = "/tms/{z}/{x}/{y}.mvt";
    var options = {
        renderFactory: L.svg.tile,
        interactive: true,
        vectorTileLayerStyles: osm_styles(),
        getFeatureId: osm_layer_id,
        attribution: 'Map data © <a href="https://www.openstreetmap.org/">OpenStreetMap</a>',
        maxNativeZoom: 18,
    };
    var osm_layers = L.vectorGrid.protobuf(osm_url, options);
    var options = {
        renderFactory: L.svg.tile,
        interactive: true,
        vectorTileLayerStyles: tms_styles(),
        getFeatureId: tms_layer_id,
        attribution: '',
        maxNativeZoom: 18,
    };
    var tms_layers = L.vectorGrid.protobuf(tms_url, options);
    var osm_select;
    function osm_on_click(e) {
        let fid = osm_layer_id(e.layer);
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
                radius: 6,
                color: 'red',
                opacity: 0.1,
            });
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
    osm_layers.on('click', osm_on_click);
    var tms_select;
    function tms_on_click(e) {
        let fid = tms_layer_id(e.layer);
        if (tms_select) {
            tms_layers.resetFeatureStyle(tms_select);
            tms_select = null;
        }
        if (fid) {
            tms_layers.setFeatureStyle(fid, {
                fill: true,
                fillOpacity: 0.8,
                fillColor: "#4aa",
                color: 'white',
                weight: 2,
            });
            tms_select = fid;
        }
        const ev = new CustomEvent("tmsevent", {
            detail: fid,
            bubbles: true,
            cancelable: true,
            composed: false,
        });
        document.querySelector('#mapid').dispatchEvent(ev);
        L.DomEvent.stop(e);
    }
    tms_layers.on('click', tms_on_click);
    osm_layers.addTo(map);
    tms_layers.addTo(map);
}

function osm_layer_id(layer) {
    return layer.properties.osm_id || layer.properties.name;
}

function tms_layer_id(layer) {
    return layer.properties.tms_id || layer.properties.name;
}

function layer_label(layer) {
    let label = null;
    let name = layer.properties.name || layer.properties.ref;
    if (name) {
        label = name;
        let station_id = layer.properties.station_id;
        if (station_id) {
            label = "<b>" + station_id + "</b> " + label;
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
