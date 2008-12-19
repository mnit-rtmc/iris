#
# RPM Spec file for IRIS
# Written by Michael Darter, December 2008
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

# Note: many args are passed in via the command line from within
# the build.xml ant file.

%define name		%{_name}
%define version		%{_version}
%define _tmppath	%{_topdir}/tmp
%define _prefix		/usr/share
%define _defaultdocdir	%{_prefix}/share/doc
%define _mandir		%{_prefix}/man
%define buildroot	%{_tmppath}/%{name}-%{version}

Name:		%{name}
Summary:	The open-source IRIS advanced traffic management system (ATMS).
Version:	%{version}
Release:	1
License:	GPL
Group:		Applications/Engineering
Provides:	%{name}
Source:		%{name}-%{version}.tar.gz
URL:		%{_url}
Buildroot:	%{buildroot}

Vendor:		%{_vendor}
Packager:	%{_vendor}
%Description
%{_rpmdesc}

# untar the source
%prep
echo "-----------Starting prep."
echo "Building RPMs for %{_vendor}"
echo name is %{name}.
echo version is %{version}.
echo _topdir is %{_topdir}.
echo Buildroot is %{buildroot}.
echo Appinstalldir is %{_appinstalldir}.
%setup -q 
echo "Done with prep in spec."

# build the app from the source
%build
echo "-----------Starting build."
ant
echo "Done with build in spec."

# install the distro files
%install
echo "-----------Starting install to %{buildroot}."
ant -Dinstall.base.dir=%{buildroot} install
echo "Done with install in spec."

# clean up the mess
%clean
echo "-----------Starting clean."
rm -rf %{buildroot}
echo "Done with clean in spec."

# All files that will be placed in the RPM are listed
# here. This includes both the client and server
%files

# /usr/share/java/iris
%defattr(0755,tms,tms)
%{_appinstalldir}

# /usr/share/java/iris
%defattr(0644,tms,tms)
%{_appinstalldir}/%{name}
%{_appinstalldir}/%{name}-rmi-%{version}.jar
%{_appinstalldir}/%{name}-server-%{version}.jar
%{_appinstalldir}/%{name}-utils-%{version}.jar
%{_appinstalldir}/mail.jar
%{_appinstalldir}/postgresql.jar
%{_appinstalldir}/scheduler-%{_version_scheduler}.jar
%{_appinstalldir}/sonar-server-%{_version_sonar}.jar
%{_appinstalldir}/vault-%{_version_vault}.jar
%{_appinstalldir}/%{name}.logging.properties

# /etc/iris
%defattr(0644,tms,tms)
/etc/iris/%{name}-server.properties
/etc/iris/%{_server_sonar_keystore}

# /usr/share/java/iris
%defattr(0744,tms,tms)
%{_appinstalldir}/run_%{name}

# /etc/rc.d/init.d
%defattr(0755,root,root)
/etc/rc.d/init.d/%{name}

# client: /var/www/html/iris-client-x.x.x
%defattr(0555,apache,apache)
/var/www/html/iris-client-%{version}/activation.jnlp
/var/www/html/iris-client-%{version}/images
/var/www/html/iris-client-%{version}/index.html
/var/www/html/iris-client-%{version}/iris.bat
/var/www/html/iris-client-%{version}/iris-client.jnlp
/var/www/html/iris-client-%{version}/iris-client.properties
/var/www/html/iris-client-%{version}/iris.sh
/var/www/html/iris-client-%{version}/lib
/var/www/html/iris-client-%{version}/mail.jnlp

# client: /var/www/html/iris-client-x.x.x/lib
%defattr(0555,apache,apache)
/var/www/html/iris-client-%{version}/lib/activation.jar
/var/www/html/iris-client-%{version}/lib/mail.jar
/var/www/html/iris-client-%{version}/lib/iris-client-%{version}.jar
/var/www/html/iris-client-%{version}/lib/datatools-%{_version_datatools}.jar
/var/www/html/iris-client-%{version}/lib/iris-rmi-%{version}.jar
/var/www/html/iris-client-%{version}/lib/iris-utils-%{version}.jar
/var/www/html/iris-client-%{version}/lib/MapBean-%{_version_mapbean}.jar
/var/www/html/iris-client-%{version}/lib/scheduler-%{_version_scheduler}.jar
/var/www/html/iris-client-%{version}/lib/Shapes-%{_version_shapes}.jar
/var/www/html/iris-client-%{version}/lib/sonar-client-%{_version_sonar}.jar
/var/www/html/iris-client-%{version}/lib/tdxml-%{_version_tdxml}.jar
/var/www/html/iris-client-%{version}/lib/tms-log-%{_version_log}.jar
/var/www/html/iris-client-%{version}/lib/TrafMap-%{_version_trafmap}.jar
/var/www/html/iris-client-%{version}/lib/video-client-%{_version_videoclient}.jar
