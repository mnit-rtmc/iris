var menu;
let menuVisible = false;

window.onclick = (e) => {
  if (e.which === 1 && menuVisible) {
    menu.style.visibility = "hidden";
    menuVisible = false;
  }
};

window.oncontextmenu = (e) => {
  e.preventDefault();
  const w = window.innerWidth;
  const h = window.innerHeight;
  const mw = menu.offsetWidth;
  const mh = menu.offsetHeight;
  const x = Math.max(Math.min(e.pageX + mw, w) - mw, 0);
  const y = Math.max(Math.min(e.pageY + mh, h) - mh, 0);
  menu.style.left = `${x}px`;
  menu.style.top = `${y}px`;
  menu.style.visibility = "visible";
  menuVisible = true;
  return false;
};



function init_map() {
    menu = document.querySelector(".menu");
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
    var segments = {
        fill: true,
        fillOpacity: 1,
        fillColor: "#282",
        stroke: false,
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
        segments: segments,
        dms: {
            radius: 4,
            fillColor: '#44d',
            fillOpacity: 1,
            fill: true,
            weight: 0.1,
            color: '#000',
        },
    };
    var options = {
        renderFactory: L.svg.tile,
        interactive: true,
        vectorTileLayerStyles: styles,
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
            var name = e.layer.properties.ref || e.layer.properties.name;
            if (typeof name != "undefined") {
                L.popup({ closeButton: false})
                 .setContent(name)
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
