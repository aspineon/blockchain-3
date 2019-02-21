# Ubuntu

Notes for setting up to build on an Ubuntu server 


## Setting up VM

Deploy a VM with Ubuntu 16.4. Suggest 8GB memory and 2 or 4 virtual CPUS 

From shell, apply the following

```bash
sudo apt-get updates
sudo apt install docker.io
sudo apt install openjdk-8-jdk

sudo groupadd docker
sudo usermod -aG docker $USER
```

restart VN and log back in

## Checkout and build Corda accelerator 

```bash
mkdir workbench
cd workbench
git clone https://github.com/<user>/<repo>
git checkout <my-branch>
cd blockchain/blockchain-development-kit/accelerators/corda
./buildAll.sh
```

or

./buildAll.sh --test