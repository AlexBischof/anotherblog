# Defines our Vagrant environment
#
# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|

  # create mgmt node
  config.vm.define :mgmt do |mgmt_config|
      mgmt_config.vm.box = "ubuntu/trusty64"
      mgmt_config.vm.hostname = "mgmt"
      mgmt_config.vm.network :private_network, ip: "10.0.15.10"
      mgmt_config.vm.provider "virtualbox" do |vb|
        vb.memory = "256"
      end
      mgmt_config.vm.provision :shell, path: "bootstrap-mgmt.sh"
  end

  # create elasticsearch
  config.vm.define :es do |es_config|
      es_config.vm.box = "ubuntu/trusty64"
      es_config.vm.hostname = "es"
      es_config.vm.network :private_network, ip: "10.0.15.12"
      es_config.vm.network :forwarded_port, guest: 9200, host: 9200
      es_config.vm.network :forwarded_port, guest: 9300, host: 9300
      es_config.vm.provider "virtualbox" do |vb|
        vb.memory = "2048"
      end
  end
end
