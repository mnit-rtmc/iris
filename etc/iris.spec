#
# RPM Spec file for IRIS
# Written by Michael Darter, December 2008
#     and Douglas Lau
#
# IRIS -- Intelligent Roadway Information System
# Copyright (C) 2009  Minnesota Department of Transportation
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
%define _serverlink	/usr/share/java/iris-server
%define _serverdir	%{_serverlink}-%{version}
%define _clientlink	/var/www/html/iris-client
%define _clientdir	%{_clientlink}-%{version}

Name:		%{name}
Summary:	The IRIS advanced traffic management system (ATMS).
Version:	%{version}
Release:	@@RPM.RELEASE@@
License:	GPL
Group:		Applications/Engineering
Provides:	%{name}
Source:		%{name}-%{version}.tar.gz
URL:		@@RPM.URL@@
Buildroot:	%{buildroot}
Vendor:		@@RPM.PACKAGER@@
Packager:	@@RPM.PACKAGER@@
Requires:	java-1.6.0-openjdk, postgresql-server, httpd

%Description
@@RPM.DESCRIPTION@@

# untar the source
%prep
echo "-----------Starting prep."
echo "Building RPMs for %{_vendor}"
echo name is %{name}.
echo version is %{version}.
echo _topdir is %{_topdir}.
echo Buildroot is %{buildroot}.
echo Installdir is %{_installdir}.
%setup -q 
echo "Done with prep in spec."

# build the app from the source
%build
echo "-----------Starting build."
ant
echo "Done with build in spec."

# install the distro files
%install
echo "-----------Starting install to %{_installdir}."
ant -Dinstall.dir=%{_installdir} install
echo "Done with install in spec."

%pre
if [ $1 == 1 ]; then
	useradd -r -M tms
	if [ "$?" == "9" ]
	then
		exit 0
	fi
fi

%post
if [ $1 == 1 ]; then
	chkconfig --add iris
fi

# All files that will be placed in the RPM are listed
# here. This includes both the client and server
%files

# /etc/iris
%dir %attr(0750,tms,tms) /etc/iris
%defattr(0640,tms,tms)
%config(noreplace) /etc/iris/iris-client.properties
%config(noreplace) /etc/iris/iris-server.properties

# /usr/bin
%defattr(0755,root,root)
/usr/bin/iris_service

# /etc/rc.d/init.d
%defattr(0755,root,root)
/etc/rc.d/init.d/iris

# /usr/share/java/iris-server-x.x.x
%dir %attr(0755,tms,tms) %{_serverdir}
%defattr(0644,tms,tms)
%{_serverdir}/iris-rmi-%{version}.jar
%{_serverdir}/iris-server-%{version}.jar
%{_serverdir}/iris-utils-%{version}.jar
%{_serverdir}/mail.jar
%{_serverdir}/postgresql.jar
%{_serverdir}/scheduler-@@SCHEDULER.VERSION@@.jar
%{_serverdir}/sonar-server-@@SONAR.VERSION@@.jar
%{_serverdir}/vault-@@VAULT.VERSION@@.jar

# /var/lib/iris
%dir %attr(0775,tms,tms) /var/lib/iris
%attr(0444,tms,tms) /var/lib/iris/sql/
%dir %attr(0775,tms,tms) /var/lib/iris/sql
%dir %attr(0775,tms,tms) /var/lib/iris/meter
%dir %attr(0775,tms,tms) /var/lib/iris/traffic
%dir %attr(0775,tms,tms) /var/lib/iris/xml

# /var/log/iris
%dir %attr(3775,tms,tms) /var/log/iris

# client: /var/www/html/iris-client-x.x.x
%dir %attr(0755,apache,apache) %{_clientdir}
%dir %attr(0755,apache,apache) %{_clientdir}/images
%dir %attr(0755,apache,apache) %{_clientdir}/lib
%defattr(0444,apache,apache)
%{_clientdir}/index.html
%{_clientdir}/mail.jnlp
%{_clientdir}/iris-client.jnlp
%{_clientdir}/images/iris.gif
%{_clientdir}/images/iris_icon.png
%{_clientdir}/lib/mail.jar
%{_clientdir}/lib/iris-client-%{version}.jar
%{_clientdir}/lib/iris-rmi-%{version}.jar
%{_clientdir}/lib/iris-utils-%{version}.jar
%{_clientdir}/lib/MapBean-@@MAPBEAN.VERSION@@.jar
%{_clientdir}/lib/scheduler-@@SCHEDULER.VERSION@@.jar
%{_clientdir}/lib/shapes-@@SHAPES.VERSION@@.jar
%{_clientdir}/lib/sonar-client-@@SONAR.VERSION@@.jar
%{_clientdir}/lib/tdxml-@@TDXML.VERSION@@.jar
%{_clientdir}/lib/tms-log-@@TMSLOG.VERSION@@.jar
%{_clientdir}/lib/TrafMap-@@TRAFMAP.VERSION@@.jar
%{_clientdir}/lib/video-client-@@VIDEOCLIENT.VERSION@@.jar
%attr(0644,tms,apache) %{_clientdir}/session_ids
