#
# Fedora RPM Spec file for IRIS
# Written by Michael Darter, December 2008
#     and Douglas Lau
#
# IRIS -- Intelligent Roadway Information System
# Copyright (C) 2009-2016  Minnesota Department of Transportation
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
Summary:	An advanced traffic management system (ATMS)
Version:	%{version}
Release:	@@RPM.RELEASE@@
License:	GPLv2+
Group:		Applications/Engineering
Source:		%{name}-%{version}.tar.gz
URL:		@@RPM.URL@@
BuildArch:	noarch
Buildroot:	%{buildroot}
Requires:	java-openjdk, postgresql-server, postgresql-jdbc, httpd

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

# pre-uninstall
%preun
%systemd_preun iris.service

# post-uninstall
%postun
%systemd_postun

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

# /etc/httpd/conf.d
%defattr(0644,root,root)
/etc/httpd/conf.d/iris.conf

# /etc/security/limits.d
%defattr(0644,root,root)
/etc/security/limits.d/99-tms.conf

# /usr/share/java/iris-server-%{version}
%defattr(0644,root,root,0755)
%dir %{_serverdir}
%{_serverdir}/iris-server-%{version}.jar
%{_serverdir}/iris-common-%{version}.jar
%{_serverdir}/mail.jar
%{_serverdir}/scheduler-@@SCHEDULER.VERSION@@.jar
%{_serverdir}/sonar-server-@@SONAR.VERSION@@.jar

# /var/lib/iris
%dir %attr(3775,tms,tms) /var/lib/iris
%attr(0444,root,root) /var/lib/iris/sql/
%dir %attr(0755,root,root) /var/lib/iris/sql
%dir %attr(0755,root,root) /var/lib/iris/sql/fonts
%dir %attr(3775,tms,tms) /var/lib/iris/meter
%dir %attr(3775,tms,tms) /var/lib/iris/traffic

# /var/log/iris
%dir %attr(3775,tms,tms) /var/log/iris

# /var/www/html/iris_xml
%dir %attr(3775,tms,tms) /var/www/html/iris_xml

# client: /var/www/html/iris-client-%{version}
%defattr(0444,apache,apache,0755)
%dir %{_clientdir}
%dir %{_clientdir}/images
%dir %{_clientdir}/lib
%{_clientdir}/index.html
%{_clientdir}/mail.jnlp
%{_clientdir}/iris-client.jnlp
%{_clientdir}/images/iris.png
%{_clientdir}/images/iris_icon.png
%{_clientdir}/lib/mail.jar
%{_clientdir}/lib/iris-client-%{version}.jar
%{_clientdir}/lib/iris-common-%{version}.jar
%{_clientdir}/lib/scheduler-@@SCHEDULER.VERSION@@.jar
%{_clientdir}/lib/sonar-client-@@SONAR.VERSION@@.jar
%attr(0644,tms,apache) %{_clientdir}/session_ids
