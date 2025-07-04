#
# Copyright (C) 2015 Wind River Systems, Inc.
#

require irqbalance.inc

SRCREV = "b3adb5fd1496833e4e2cebc958d3919178cd5e3d"
PV = "1.8.0+git"

SRC_URI = "git://github.com/Irqbalance/irqbalance;branch=master;protocol=https \
           file://add-initscript.patch \
           file://irqbalance-Add-status-and-reload-commands.patch \
           file://0001-add-void-to-fix-strict-prototypes.patch \
          "

CFLAGS += "-Wno-error=format-security"
