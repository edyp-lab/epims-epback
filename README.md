# epims-epback
Software used to transfer new acquisitions from an Instrument PC to the ePims repository.  
It communicates with ePims2 Server which fills in the database and sends a JMS message to eP-Taf.

Previously hosted on CEA Tuleap Projects.


## Revisions

### version 2.3.6

* Added acquisition properties (Category, methodName, injectionVolume and vialInformation) in Analysis interface and implem. Send this information through AcquisitionJson.
* Update dependnceis di msdata converter

### version ... 2.3.5

* Allow FTP file transfer
* Use server Rest API to log instruments information
* Allow more FTP configuration (port/authentification mode)
* Add TimsTof Analysis support
* Use ThermoAccess (based on Thermo lib) to read ThermoFisher metadata
* Refactoring

### version 2.0.X - 2.1.x - 2.2.x

* Support Nems instruments
* Use new SpringBoot ePims Server
* Disallow to change configuration during processing of analysis

