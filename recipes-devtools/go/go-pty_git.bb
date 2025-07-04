DESCRIPTION = "PTY interface for Go"
HOMEPAGE = "https://github.com/creack/pty"
SECTION = "devel/go"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://License;md5=93958070863d769117fa33b129020050"

SRCNAME = "pty"

PKG_NAME = "github.com/creack/${SRCNAME}"
SRC_URI = "git://${PKG_NAME}.git;branch=master;protocol=https"

SRCREV = "05017fcccf23c823bfdea560dcc958a136e54fb7"

inherit meta-virt-depreciated-warning

do_install() {
	install -d ${D}${prefix}/local/go/src/${PKG_NAME}
	cp -r ${S}/* ${D}${prefix}/local/go/src/${PKG_NAME}/
}

SYSROOT_PREPROCESS_FUNCS += "go_pty_sysroot_preprocess"

go_pty_sysroot_preprocess () {
    install -d ${SYSROOT_DESTDIR}${prefix}/local/go/src/${PKG_NAME}
    cp -r ${D}${prefix}/local/go/src/${PKG_NAME} ${SYSROOT_DESTDIR}${prefix}/local/go/src/$(dirname ${PKG_NAME})
}

FILES:${PN} += "${prefix}/local/go/src/${PKG_NAME}/*"
