HOMEPAGE = "https://buildah.io"
SUMMARY = "A tool that facilitates building OCI container images."
DESCRIPTION = "A tool that facilitates building OCI container images."

# Apache-2.0 for containerd
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://src/github.com/containers/buildah/LICENSE;md5=e3fc50a88d0a364313df4b21ef20c29e"

BUILDAH_VERSION = "1.40.0"

PV = "${BUILDAH_VERSION}"

inherit go
inherit goarch
inherit pkgconfig

# Rdepends on podman which needs seccomp and ipv6
inherit features_check
REQUIRED_DISTRO_FEATURES = "seccomp ipv6"

COMPATIBLE_HOST = "^(?!mips).*"

GO_IMPORT = "github.com/containers/buildah"
GO_INSTALL = "${GO_IMPORT}"
GO_WORKDIR = "${GO_INSTALL}"
GOBUILDFLAGS += "-mod vendor"

SRCREV = "ed56ef16dd75c3e8d53f93165d5f77e734944fe1"

SRC_URI = " \
    git://github.com/containers/buildah;branch=release-1.40;name=buildah;protocol=https;destsuffix=${GO_SRCURI_DESTSUFFIX} \
    "

DEPENDS = "libdevmapper btrfs-tools gpgme"
RDEPENDS:${PN} = "cgroup-lite fuse-overlayfs libdevmapper podman"
RDEPENDS:${PN}-dev = "bash perl"

do_compile:prepend() {
    cd ${S}/src/github.com/containers/buildah
}

go_do_compile() {
        export TMPDIR="${GOTMPDIR}"
        export AS='${CC} -c'
        if [ -n "${GO_INSTALL}" ]; then
                ${GO} install ${GOBUILDFLAGS} ./cmd/buildah
                ${GO} install ${GOBUILDFLAGS} ./tests/imgtype/imgtype.go
                ${GO} install ${GOBUILDFLAGS} ./tests/copy/copy.go
        fi

        # x86 statically linked executable that we don't need
        rm -f ${S}/src/github.com/containers/buildah/internal/mkcw/embed/entrypoint
}

do_install:append() {
    dest_dir=${D}/${sysconfdir}/containers
    mkdir -p ${dest_dir}
    install -m 666 ${S}/src/github.com/containers/buildah/docs/samples/registries.conf ${dest_dir}/buildah.registries.conf.sample
    install -m 666 ${S}/src/github.com/containers/buildah/tests/policy.json ${dest_dir}/buildah.policy.json.sample
}

INSANE_SKIP:${PN} = "already-stripped"
