# Only use this version for sky builds...
DEFAULT_PREFERENCE = "${@bb.utils.contains('DISTRO_FEATURES' , 'build_for_sky', '1', '-99', d)}"

HOMEPAGE = "https://github.com/opencontainers/runc"
SUMMARY = "runc container cli tools"
DESCRIPTION = "runc is a CLI tool for spawning and running containers according to the OCI specification."

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=435b266b3899aa8a959f17d41c56def8"

SRC_URI = "\
    git://github.com/opencontainers/runc;branch=master;destsuffix=${S} \
    "
SRCREV = "d736ef14f0288d6993a1845745d6756cfc9ddd5a"
RUNC_VERSION = "1.0.0-rc9"
 
GOPATH = "${WORKDIR}/gopath/"
S = "${GOPATH}/src/github.com/opencontainers/runc"

inherit go-osarchmap

DEPENDS = "go-cross"

EXTRA_OEMAKE="BUILDTAGS=''"

do_compile() {
    export GOARCH="${TARGET_GOARCH}"
 
    # Set GOPATH. See 'PACKAGERS.md'. Don't rely on
    # docker to download its dependencies but rather
    # use dependencies packaged independently.
 
    export GOPATH="${GOPATH}"
    export GOROOT="${STAGING_LIBDIR_NATIVE}/${TARGET_SYS}/go"
    cd "${S}"
 
    # Pass the needed cflags/ldflags so that cgo
    # can find the needed headers files and libraries
    export CGO_ENABLED="1"
    export CGO_CFLAGS="${CFLAGS} --sysroot=${STAGING_DIR_TARGET}"
    export CGO_LDFLAGS="${LDFLAGS} --sysroot=${STAGING_DIR_TARGET}"
    export CFLAGS=""
    export LDFLAGS=""
 
    oe_runmake static
}

do_install() {
        install -d ${D}${bindir}
        install -m 0755 ${S}/runc ${D}/${bindir}
}


