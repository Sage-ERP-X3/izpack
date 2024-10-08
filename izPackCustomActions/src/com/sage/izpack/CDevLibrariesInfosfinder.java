package com.sage.izpack;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Resources;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 *
 * @author ogattaz
 *
 */
public class CDevLibrariesInfosfinder extends CAbstractOsInfosfinder {

	private final static String PREREQUISITES_SCRIPT = "PrerequisitesControlLibScript"; // "PrerequisitesControlScript.sh";

	/**
	 * @param aReport
	 */
	public CDevLibrariesInfosfinder(final CReport pReport, InstallData pData, Resources resources) {
		super(pReport, pData, resources);
	}

	/*
	 * @override
	 */
	@Override
	public String getResourceName() {
		return PREREQUISITES_SCRIPT + "_" + PLATFORM;
	}

	/**
	 * <pre>
		[root@localhost httpd-src]# yum list installed '*-devel*'
		Installed Packages
		apr-devel.x86_64                                                    1.6.3-11.el8                                         @ol8_appstream
		apr-util-devel.x86_64                                               1.6.1-6.el8                                          @ol8_appstream
		binutils-devel.x86_64                                               2.30-79.0.1.el8                                      @ol8_appstream
		cyrus-sasl-devel.x86_64                                             2.1.27-5.el8                                         @ol8_baseos_latest
		expat-devel.x86_64                                                  2.2.5-4.el8                                          @ol8_baseos_latest
		gettext-common-devel.noarch                                         0.19.8.1-17.el8                                      @ol8_baseos_latest
		gettext-devel.x86_64                                                0.19.8.1-17.el8                                      @ol8_baseos_latest
		glibc-devel.x86_64                                                  2.28-127.0.3.el8_3.2                                 @ol8_baseos_latest
		httpd-devel.x86_64                                                  2.4.37-30.0.1.module+el8.3.0+7816+49791cfd           @ol8_appstream
		kernel-uek-devel.x86_64                                             5.4.17-2011.7.4.el8uek                               @ol8_UEKR6
		kernel-uek-devel.x86_64                                             5.4.17-2102.200.13.el8uek                            @ol8_UEKR6
		kernel-uek-devel.x86_64                                             5.4.17-2102.201.3.el8uek                             @ol8_UEKR6
		libcurl-devel.x86_64                                                7.61.1-14.el8_3.1                                    @ol8_baseos_latest
		libdb-devel.x86_64                                                  5.3.28-39.el8                                        @ol8_appstream
		libstdc++-devel.x86_64                                              8.3.1-5.1.0.2.el8                                    @ol8_appstream
		libxcrypt-devel.x86_64                                              4.1.1-4.el8                                          @ol8_baseos_latest
		libxml2-devel.x86_64                                                2.9.7-8.0.1.el8                                      @ol8_appstream
		openldap-devel.x86_64                                               2.4.46-15.el8                                        @ol8_baseos_latest
		pcre-devel.x86_64                                                   8.42-4.el8                                           @ol8_baseos_latest
		perl-Devel-PPPort.x86_64                                            3.36-5.el8                                           @ol8_appstream
		perl-Devel-Peek.x86_64                                              1.26-417.el8_3                                       @ol8_appstream
		perl-Devel-SelfStubber.noarch                                       1.06-417.el8_3                                       @ol8_appstream
		perl-Devel-Size.x86_64                                              0.81-2.el8                                           @ol8_appstream
		perl-Encode-devel.x86_64                                            4:2.97-3.el8                                         @ol8_appstream
		perl-devel.x86_64                                                   4:5.26.3-417.el8_3                                   @ol8_appstream
		systemtap-devel.x86_64                                              4.3-4.0.1.el8                                        @ol8_appstream
		valgrind-devel.x86_64                                               1:3.16.0-2.el8                                       @ol8_appstream
		xz-devel.x86_64                                                     5.2.4-3.el8                                          @ol8_baseos_latest
		zlib-devel.x86_64                                                   1.2.11-16.2.el8_3                                    @ol8_baseos_latest

		[root@localhost httpd-src]# yum list installed 'libxml2*'
		Installed Packages
		libxml2.x86_64                                                       2.9.7-8.0.1.el8                                     @anaconda
		libxml2-devel.x86_64
	 * </pre>
	 */

}
