#
# Fedora RPM Spec file for IRIS
# Written by Michael Darter, December 2008
#     and Douglas Lau
#
# IRIS -- Intelligent Roadway Information System
# Copyright (C) 2009-2025  Minnesota Department of Transportation
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

%define name		@@NAME@@
%define version		@@VERSION@@
%define _topdir		@@BUILD.RPM@@
%define _installdir	%{_topdir}/BUILDROOT
%define _serverdir	/usr/share/java/iris-server
%define _clientdir	/var/www/html/iris-client
%define _source_payload w6.xzdio
%define _binary_payload w6.xzdio

Name:		%{name}
Summary:	An advanced traffic management system (ATMS)
Version:	%{version}
Release:	@@RPM.RELEASE@@
License:	GPLv2+
Group:		Applications/Engineering
Source:		%{name}-%{version}.tar.gz
URL:		@@RPM.URL@@
BuildArch:	noarch
Buildroot:	%{buildroot}
Requires:	(java-openjdk or java-1.8.0-openjdk), postgresql-server, postgresql-jdbc, postgis-utils osm2pgsql, nginx

%Description
@@RPM.DESCRIPTION@@

# prepare sources
%prep
%setup -q

# build from source
%build
ant dist

# install the built files
%install
ant -Dinstall.dir=%{_installdir} install

# pre-install
%pre
if [ $1 == 1 ]; then
	useradd -r -m tms
	# exit value 9: username already in use
	if [ "$?" == "9" ]; then
		exit 0
	fi
fi

# post-install or upgrade
%post
%systemd_post iris.service
# Workaround bug 1358476
# Cause: Bad interaction between SELinux and rpath (used to locate libjli)
# Symptom: IRIS server process inactive (dead) with an exit status of 127
ln -sf /usr/lib/jvm/jre-openjdk/lib/amd64/jli/libjli.so /usr/lib64

# pre-uninstall
%preun
%systemd_preun iris.service

# post-uninstall
%postun
%systemd_postun iris.service

# All files included in RPM are listed here.
%files

%doc COPYING

# /etc/iris
%defattr(0640,tms,tms,0750)
%dir /etc/iris
%config(noreplace) /etc/iris/iris-client.properties
%config(noreplace) /etc/iris/iris-server.properties

# /usr/bin
%defattr(0755,root,root)
/usr/bin/iris_ctl

# %{_unitdir}
%defattr(0644,root,root)
%{_unitdir}/iris.service

# /etc/nginx/
%defattr(0644,root,root)
/etc/nginx/conf.d/nginx-iris-cache.conf
/etc/nginx/default.d/nginx-iris.conf
/etc/nginx/default.d/nginx-rest.conf

# /etc/security/limits.d
%defattr(0644,root,root)
/etc/security/limits.d/99-tms.conf

# /usr/share/java/iris-server
%defattr(0644,root,root,0755)
%dir %{_serverdir}
%{_serverdir}/iris-server-%{version}.jar
%{_serverdir}/iris-common-%{version}.jar
%{_serverdir}/json-@@JSON.VERSION@@.jar
%{_serverdir}/postgis-jdbc-@@POSTGIS.VERSION@@.jar
%{_serverdir}/postgis-geometry-@@POSTGIS.VERSION@@.jar
%{_serverdir}/jsch-@@JSCH.VERSION@@.jar

# /var/cache/nginx/earthwyrm-iris
%dir %attr(0755,nginx,nginx) /var/cache/nginx
%dir %attr(0755,nginx,nginx) /var/cache/nginx/earthwyrm-iris

# /var/lib/iris
%dir %attr(0775,tms,tms) /var/lib/iris
%dir %attr(0775,tms,tms) /var/lib/iris/backup
%dir %attr(0775,tms,tms) /var/lib/iris/meter
%dir %attr(0755,tms,tms) /var/lib/iris/sql
%attr(0444,tms,tms) /var/lib/iris/sql/*.sql
%dir %attr(0755,tms,tms) /var/lib/iris/tfon
%attr(0444,tms,tms) /var/lib/iris/tfon/*.tfon
%dir %attr(0775,tms,tms) /var/lib/iris/traffic
%dir %attr(0775,tms,tms) /var/lib/iris/web
%attr(0444,tms,tms) /var/lib/iris/web/index.html
%dir %attr(0775,tms,tms) /var/lib/iris/web/bulb
%attr(0444,tms,tms) /var/lib/iris/web/bulb/*

# /var/log/iris
%dir %attr(3775,tms,tms) /var/log/iris

# /var/www/html/
%attr(0644,root,root) /var/www/html/index.html
%dir %attr(3775,tms,tms) /var/www/html/iris_xml
%dir %attr(3775,tms,tms) /var/www/html/iris-gstreamer

# client: /var/www/html/iris-client
%defattr(0444,root,root,0755)
%dir %{_clientdir}
%dir %{_clientdir}/images
%dir %{_clientdir}/lib
%{_clientdir}/index.html
%{_clientdir}/iris-client.jnlp
%{_clientdir}/images/iris.png
%{_clientdir}/images/iris_icon.png
%{_clientdir}/lib/jna-@@JNA.VERSION@@.jar
%{_clientdir}/lib/jna-platform-@@JNA.VERSION@@.jar
%{_clientdir}/lib/gst1-java-core-@@GST.JAVA.VERSION@@.jar
%{_clientdir}/lib/json-@@JSON.VERSION@@.jar
%{_clientdir}/lib/postgis-geometry-@@POSTGIS.VERSION@@.jar
%{_clientdir}/lib/iris-client-%{version}.jar
%{_clientdir}/lib/iris-common-%{version}.jar
%attr(0644,tms,tms) %{_clientdir}/session_ids
