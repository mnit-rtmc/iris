function segment_style(properties, zoom) {
    var opacity = 1;
    var color = "#282";
    if (typeof properties.station == "undefined") {
        opacity = 0.8;
        color = "#666";
    }
    return {
        fill: true,
        fillOpacity: opacity,
        fillColor: color,
        stroke: false,
    };
}

function make_styles() {
    var boundary = {
        fill: true,
        fillOpacity: 0.2,
        weight: 0.1,
        color: '#000000',
        opacity: 0.6,
    };
    var water = {
        fill: true,
        fillOpacity: 0.8,
        fillColor: "#b5d0d0",
        stroke: false,
    };
    var wetland = {
        fill: true,
        fillOpacity: 0.8,
        fillColor: "#b8d0bd",
        stroke: false,
    };
    var leisure = {
        fill: true,
        fillOpacity: 0.6,
        fillColor: "#88cc88",
        weight: 0.1,
        color: '#000000',
        opacity: 0.6,
    };
    var building = {
        fill: true,
        fillOpacity: 0.7,
        fillColor: "#bca9a9",
        weight: 0.7,
        color: "#baa",
    };
    var retail = {
        fill: true,
        fillOpacity: 0.25,
        fillColor: "#b99",
        stroke: false,
    };
    var parking = {
        fill: true,
        fillOpacity: 0.6,
        fillColor: "#cca",
        stroke: false,
        weight: 1,
    };
    var styles = {
        county: Object.assign(boundary, { fillColor: '#f8f4f2' }),
        city: Object.assign(boundary, { fillColor: '#f1eee8' }),
        lake: water,
        river: water,
        water: water,
        pond: water,
        basin: water,
        wetland: wetland,
        leisure: leisure,
        retail: retail,
        motorway: { color: "#ffd9a9", weight: 5 },
        trunk: { color: "#ffe0a9" },
        primary: { color: "#ffeaa9" },
        secondary: { color: "#fff4a9" },
        tertiary: { color: "#ffffa9", weight: 2 },
        roads: { color: "#eee", weight: 1.5 },
        paths: { color: "#333", weight: 1, dashArray: "1 3" },
        building: building,
        parking: parking,
        segments: segment_style,
    };
    return styles;
}

function init_map() {
    var map = L.map('mapid', {
        center: [45, -93],
        zoom: 12,
    });
    var osm_url = "http://127.0.0.1/tile/{z}/{x}/{y}.mvt";
    var tms_url = "http://127.0.0.1/tms/{z}/{x}/{y}.mvt";
    var highlight_style = {
        fill: true,
        fillColor: 'red',
        fillOpacity: 0.1,
        radius: 6,
        color: 'red',
        opacity: 0.1,
    };
    var options = {
        renderFactory: L.svg.tile,
        interactive: true,
        vectorTileLayerStyles: make_styles(),
        getFeatureId: function(feat) {
            return feat.properties.osm_id || feat.properties.sid;
        },
        attribution: 'Map data © <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>',
        maxNativeZoom: 18,
    };
    var highlight;
    var osm_layers = L.vectorGrid.protobuf(osm_url, options);
    var tms_layers = L.vectorGrid.protobuf(tms_url, options);
    function on_click(e) {
        var osm_id = e.layer.properties.osm_id || e.layer.properties.sid;
        var change = (typeof osm_id != "undefined") && (osm_id != highlight);
        if (highlight) {
            osm_layers.resetFeatureStyle(highlight);
            tms_layers.resetFeatureStyle(highlight);
            highlight = null;
        }
        if (change) {
            highlight = osm_id;
            osm_layers.setFeatureStyle(highlight, highlight_style);
            tms_layers.setFeatureStyle(highlight, highlight_style);
            var name = e.layer.properties.name || e.layer.properties.ref;
            if (typeof name != "undefined") {
                var content = name;
                if (typeof e.layer.properties.station != "undefined")
                    content += "<br/>" + e.layer.properties.station;
                L.popup({ closeButton: false})
                 .setContent(content)
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

window.onload = init_map;
