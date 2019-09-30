# Mapping

The IRIS client can use map tiles to display high-quality maps as a background
layer.  The map tiles are fetched from a web server on demand as the client map
is panned and zoomed.  The tiles can be generated from [OpenStreetMap] data.
The following instructions describe one method of generating the map tiles on
Fedora 29.

## OSM Data

Download the latest OSM data from [Geofabrik].  Find the sub-region of interest
— for example, data for Minnesota is [here].  This file is 164 MB as of December
2018.

### Import OSM Data

The time required to for this process varies depending on the amount of data.
For Minnesota, it takes about 5 minutes on a beefy server.

```
su --login postgres
time osm2pgsql -v --number-processes=8 -d osm -s --drop --multi-geometry ./minnesota-20181219.osm.pbf
```

### Mapnik

See the [mapnik installation] documentation.  The [Springmeyer documentation] is
also very useful.

```
dnf install mapnik
```

See [mapnik tools] documentation.

```
sudo dnf -y install svn
cd ~/osm
svn export http://svn.openstreetmap.org/applications/rendering/mapnik
# Install prepared world boundary data
cd ~/osm/mapnik
mkdir world_boundaries
wget http://tile.openstreetmap.org/world_boundaries-spherical.tgz
tar xvzf world_boundaries-spherical.tgz
wget http://tile.openstreetmap.org/processed_p.tar.bz2
tar xvjf processed_p.tar.bz2 -C world_boundaries
wget http://tile.openstreetmap.org/shoreline_300.tar.bz2
tar xjf shoreline_300.tar.bz2 -C world_boundaries
wget http://www.naturalearthdata.com/http//www.naturalearthdata.com/download/10m/cultural/ne_10m_populated_places.zip
unzip ne_10m_populated_places.zip -d world_boundaries
wget http://www.naturalearthdata.com/http//www.naturalearthdata.com/download/110m/cultural/ne_110m_admin_0_boundary_lines_land.zip
unzip ne_110m_admin_0_boundary_lines_land.zip  -d world_boundaries
```

### Generate tiles

Generating tiles takes about 2 hours on a 16 processor system, or about 13K
tiles/minute.

```
su - tms
cd ~/osm/mapnik
export MAPNIK_MAP_FILE=~/osm/mapnik/iris_osm.xml
export MAPNIK_TILE_DIR=/home/tms/osm/tiles
# Customize osm.xml
./generate_xml.py  --dbname osm --accept-none
vim generate_tiles_multiprocess.py
	NUM_THREADS = number of processors in your server
	bbox = (-97.5, 43.0, -89.0, 49.5)
	minZoom = 6
	maxZoom = 16
	# comment all of the european cities listed
time ./generate_tiles_multiprocess.py
# Copy tiles to server hosting them
scp -rp ~/osm/tiles/* root@tilehost:/var/www/html/
```

## Map Extents

Select `View ➔ System ➔ Map Extents` menu item

This form enables the creation of map extents, which are buttons allowing quick
selection of a map location and zoom level.  Each map extent will be displayed
as a button on the upper right area of the map (next to the zoom in/out
buttons).  First, a `Home` extent should be created, which is selected by
default when a user logs in.  Other extents can also be created for commonly
selected map spots.


[Geofabrik]: http://download.geofabrik.de
[here]: http://download.geofabrik.de/north-america/us/minnesota.html
[mapnik installation]: https://github.com/mapnik/mapnik/wiki/Mapnik-Installation
[mapnik tools]: https://github.com/mapnik/mapnik/blob/v2.2.0/INSTALL.md
[OpenStreetMap]: http://openstreetmap.org
[Springmeyer documentation]: https://gist.github.com/springmeyer/3427021
