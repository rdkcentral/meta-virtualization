# Only use this version for sky builds...
DEFAULT_PREFERENCE = "${@bb.utils.contains('DISTRO_FEATURES' , 'build_for_sky', '1', '-99', d)}"

SUMMARY = "Go programming language compiler"
DESCRIPTION = " The Go programming language is an open source project to make \
 programmers more productive. Go is expressive, concise, clean, and\
 efficient. Its concurrency mechanisms make it easy to write programs\
 that get the most out of multicore and networked machines, while its\
 novel type system enables flexible and modular program construction.\
 Go compiles quickly to machine code yet has the convenience of\
 garbage collection and the power of run-time reflection. It's a\
 fast, statically typed, compiled language that feels like a\
 dynamically typed, interpreted language."
HOMEPAGE = " http://golang.org/"
LICENSE = "BSD-3-Clause"

inherit go-osarchmap cross

# libgcc is required for the target specific libraries to build properly
DEPENDS += "go-native libgcc"

# Prevent runstrip from running because you get errors when the host arch != target arch
#INHIBIT_PACKAGE_STRIP = "1"
STRIP = "echo"

INHIBIT_PACKAGE_DEBUG_SPLIT = "1"


PV = "1.7.6"
GO_BASEVERSION = "1.7"
FILESEXTRAPATHS_prepend := "${FILE_DIRNAME}/go-${GO_BASEVERSION}:"

SRC_URI += "http://golang.org/dl/go${PV}.src.tar.gz \
       	    file://armhf-elf-header.patch \
            file://syslog.patch \
            file://fix-target-cc-for-build.patch \
            file://fix-cc-handling.patch \
            file://split-host-and-target-build.patch \
            file://gotooldir.patch \
"

S = "${WORKDIR}/go"
B = "${S}"

LIC_FILES_CHKSUM = "file://LICENSE;md5=5d4950ecb7b26d2c5e4e7b4e0dd74707"
SRC_URI[md5sum] = "178724be1c922d8cdc4e694b6d5994ad"
SRC_URI[sha256sum] = "1a67a4e688673fdff7ba41e73482b0e59ac5bd0f7acf703bc6d50cc775c5baba"

export GOHOSTOS = "${BUILD_GOOS}"
export GOHOSTARCH = "${BUILD_GOARCH}"
export GOOS = "${TARGET_GOOS}"
export GOARCH = "${TARGET_GOARCH}"
export GOARM = "${TARGET_GOARM}"
export GOROOT_BOOTSTRAP = "${STAGING_LIBDIR_NATIVE}/go"
export GOROOT_FINAL = "${libdir}/go"
export CGO_ENABLED = "1"
export CC_FOR_TARGET="${TARGET_PREFIX}gcc ${TARGET_CC_ARCH} --sysroot=${STAGING_DIR_TARGET}"
export CXX_FOR_TARGET="${TARGET_PREFIX}g++ ${TARGET_CC_ARCH} --sysroot=${STAGING_DIR_TARGET}"
CC = "${@d.getVar('BUILD_CC', True).strip()}"

do_configure[noexec] = "1"

do_compile() {
    export GOBIN="${B}/bin"
    rm -rf ${GOBIN} ${B}/pkg
    mkdir ${GOBIN}

    export TMPDIR=${WORKDIR}/build-tmp
    mkdir -p ${WORKDIR}/build-tmp

    cd src
    ./make.bash --host-only
    # Ensure cgo.a is built with the target toolchain
    export GOBIN="${B}/target/bin"
    rm -rf ${GOBIN}
    mkdir -p ${GOBIN}
    GO_FLAGS="-a" ./make.bash
}

do_install() {
    install -d ${D}${libdir}/go
    cp -a ${B}/pkg ${D}${libdir}/go/
    install -d ${D}${libdir}/go/src
    (cd ${S}/src; for d in *; do \
        [ -d $d ] && cp -a ${S}/src/$d ${D}${libdir}/go/src/; \
    done)
    install -d ${D}${bindir}

    install -m755 ${B}/bin/go ${D}${bindir}
    install -m755 ${B}/bin/gofmt ${D}${bindir}
    install -m755 ${B}/bin/linux_arm/go ${D}${bindir}/linux_arm
    install -m755 ${B}/bin/linux_arm/gofmt ${D}${bindir}/linux_arm
}

do_package[noexec] = "1"
do_packagedata[noexec] = "1"
do_package_write_ipk[noexec] = "1"
do_package_write_deb[noexec] = "1"
do_package_write_rpm[noexec] = "1"

