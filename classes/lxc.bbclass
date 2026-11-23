LXC_PACKAGES ?= "${PN}"
LXC_TEMPLATE ?= "lxc-xre"
LXC_NAME ?= "xre"
LXC_PATH = "/lxc"
LXC_DISABLE_DLOG_DEMON ?= ""
LXC_DISABLE_DLOG_FILE ?= ""
LXC_LOG_PATH ?= "/opt/logs"
LXC_LOG_LEVEL ?= "2"

python __anonymous() {
    if "container" in d.getVar('MACHINEOVERRIDES', True):
        d.appendVar("DEPENDS", " lxc-native")
}

lxc_postinst() {

rootDir="$D"

if type lxc-create >/dev/null 2>/dev/null; then

    mkdir -p ${rootDir}${LXC_PATH}
    echo "Executing : lxc-create -t ${rootDir}/usr/share/lxc/templates/${LXC_TEMPLATE} -n ${LXC_NAME} -P ${rootDir}${LXC_PATH} -f ${NATIVE_ROOT}/${sysconfdir}/lxc/default.conf"

    lxc-create -t ${rootDir}/usr/share/lxc/templates/${LXC_TEMPLATE} -n ${LXC_NAME} -P ${rootDir}${LXC_PATH} -f ${NATIVE_ROOT}/${sysconfdir}/lxc/default.conf

    if [ $? -ne 0 ]; then
        echo "ERROR - Failed to create LXC container : ${LXC_NAME}"
        exit 1
    fi

    if [ -f "${rootDir}${LXC_PATH}/${LXC_NAME}/config" ];then

        ## Replace the rootfs path in config file  to  target runtime rootfs path
        sed -i 's|'${rootDir}${LXC_PATH}'|'${LXC_PATH}'|g' "${rootDir}${LXC_PATH}/${LXC_NAME}/config"

        ## Add Device specific include path in config.
        sed -i '/lxc.hook/i\
lxc.include = /usr/share/lxc/config/device.conf' ${rootDir}${LXC_PATH}/${LXC_NAME}/config

        ## Add log file in container config
        sed -i '/lxc.hook/i\
lxc.logfile=${LXC_LOG_PATH}/${LXC_NAME}.log' ${rootDir}${LXC_PATH}/${LXC_NAME}/config
        sed -i '/lxc.hook/i\
lxc.loglevel=${LXC_LOG_LEVEL}' ${rootDir}${LXC_PATH}/${LXC_NAME}/config

        ## Add CONTAINER_SUPPORT flag to true
        grep -q "CONTAINER_SUPPORT" ${rootDir}/etc/device.properties || sed -i '$ a\
CONTAINER_SUPPORT=true' ${rootDir}/etc/device.properties

        ## Remove module log from  dump log script
        if [ "x${LXC_DISABLE_DLOG_DEMON}" != "x" ]  && [ "x${LXC_DISABLE_DLOG_FILE}" != "x" ];then
            if [ -f "${rootDir}/lib/rdk/dumpLogs.sh" ];then
                sed -i 's|'${LXC_DISABLE_DLOG_DEMON}'| |g' "${rootDir}/lib/rdk/dumpLogs.sh"
                sed -i 's|'\${log_prefix}/${LXC_DISABLE_DLOG_FILE}'| |g' "${rootDir}/lib/rdk/dumpLogs.sh"
            fi
        fi

        ## update lxc.service file with conditional flag
        if [ -f "${rootDir}/lib/systemd/system/lxc.service" ];then
            sed -i '/Description=/a\
ConditionPathExists=!/opt/lxc_service_disabled' ${rootDir}/lib/systemd/system/lxc.service
        fi

        ## update xre-receiver.service with conditional flag
        if [ -f "${rootDir}/lib/systemd/system/xre-receiver.service" ];then
            sed -i '/Description=/a\
ConditionPathExists=/opt/lxc_service_disabled' ${rootDir}/lib/systemd/system/xre-receiver.service
        fi


    fi

fi
}

lxc_populate_packages[vardeps] += "lxc_postinst"
lxc_populate_packages[vardepsexclude] += "OVERRIDES"

python lxc_populate_packages() {
    for pkg in d.getVar('LXC_PACKAGES', True).split():
        postinst = d.getVar('pkg_postinst_%s' % pkg, True)
        if not postinst:
            postinst = '#!/bin/sh\n'
            postinst += d.getVar('lxc_postinst', True)
            d.setVar('pkg_postinst_%s' % pkg, postinst)
}

PACKAGESPLITFUNCS_prepend = "lxc_populate_packages "
FILES_${PN} += "/lxc/*"
