# Changelog DMASON 3.0.2

### 1. Package Refactoring

		.
		├── main
		│   └── java
		│       └── it
		│           └── isislab
		│               └── dmason
		│                   ├── annotation
		│                   │   ├── authorannotation.java
		│                   │   ├── batchannotation.java
		│                   │   ├── package.html
		│                   │   ├── thinannotation.java
		│                   │   └── valueannotation.java
		│                   ├── exception
		│                   │   ├── dmasonexception.java
		│                   │   ├── nodigestfoundexception.java
		│                   │   └── package.html
		│                   ├── sim
		│                   │   ├── app
		│                   │   │   ├── dantsforage
		│                   │   │   │   ├── dantsforage.java
		│                   │   │   │   ├── dantsforagewithui.java
		│                   │   │   │   ├── dremoteant.java
		│                   │   │   │   ├── icon.png
		│                   │   │   │   ├── index.html
		│                   │   │   │   ├── package.html
		│                   │   │   │   ├── remoteant.java
		│                   │   │   │   ├── teststart.java
		│                   │   │   │   └── teststartonegui.java
		│                   │   │   ├── dantsforagethin
		│                   │   │   │   ├── dantsforage.java
		│                   │   │   │   ├── dantsforagewithui.java
		│                   │   │   │   ├── dremoteant.java
		│                   │   │   │   ├── icon.png
		│                   │   │   │   ├── index.html
		│                   │   │   │   ├── package.html
		│                   │   │   │   ├── remoteant.java
		│                   │   │   │   └── teststart.java
		│                   │   │   ├── dbreadthfirstsearch
		│                   │   │   │   ├── dvertexstate.java
		│                   │   │   │   ├── mpiamqvertexes.java
		│                   │   │   │   ├── mpimpivertexes.java
		│                   │   │   │   ├── package.html
		│                   │   │   │   ├── remotevertex.java
		│                   │   │   │   ├── teststart.java
		│                   │   │   │   ├── vertexes.java
		│                   │   │   │   ├── vertexeswithui.java
		│                   │   │   │   └── vertex.java
		│                   │   │   ├── dflockers
		│                   │   │   │   ├── dflocker.java
		│                   │   │   │   ├── dflockers.java
		│                   │   │   │   ├── dflockerswithui.java
		│                   │   │   │   ├── icon.png
		│                   │   │   │   ├── icored.png
		│                   │   │   │   ├── index.html
		│                   │   │   │   ├── mpiguiworker.java
		│                   │   │   │   ├── mpiworker.java
		│                   │   │   │   ├── package.html
		│                   │   │   │   ├── remoteflock.java
		│                   │   │   │   └── teststart.java
		│                   │   │   ├── dflockersstate
		│                   │   │   │   ├── dflocker.java
		│                   │   │   │   ├── dflockers.java
		│                   │   │   │   ├── dflockerstate.java
		│                   │   │   │   ├── dflockerswithui.java
		│                   │   │   │   ├── icon.png
		│                   │   │   │   ├── icored.png
		│                   │   │   │   ├── index.html
		│                   │   │   │   ├── mpiguiworker.java
		│                   │   │   │   ├── mpiworker.java
		│                   │   │   │   ├── package.html
		│                   │   │   │   ├── remoteflock.java
		│                   │   │   │   └── teststart.java
		│                   │   │   ├── dflockersthin
		│                   │   │   │   ├── dflocker.java
		│                   │   │   │   ├── dflockers.java
		│                   │   │   │   ├── dflockerswithui.java
		│                   │   │   │   ├── icon.png
		│                   │   │   │   ├── icored.png
		│                   │   │   │   ├── index.html
		│                   │   │   │   ├── package.html
		│                   │   │   │   ├── remoteflock.java
		│                   │   │   │   └── teststart.java
		│                   │   │   ├── dnetworksir2015
		│                   │   │   │   ├── dvertexstate15.java
		│                   │   │   │   ├── mpiamqvertexes15.java
		│                   │   │   │   ├── mpimpivertexes15.java
		│                   │   │   │   ├── remotevertex15.java
		│                   │   │   │   ├── teststart15.java
		│                   │   │   │   ├── vertex15.java
		│                   │   │   │   ├── vertexes15.java
		│                   │   │   │   └── vertexeswithui15.java
		│                   │   │   ├── dnetworktest
		│                   │   │   │   ├── dvertexstate.java
		│                   │   │   │   ├── mason
		│                   │   │   │   │   ├── package.html
		│                   │   │   │   │   ├── remotevertex.java
		│                   │   │   │   │   ├── vertexes.java
		│                   │   │   │   │   ├── vertexeswithui.java
		│                   │   │   │   │   └── vertex.java
		│                   │   │   │   ├── mpiamqvertexes.java
		│                   │   │   │   ├── mpimpivertexes.java
		│                   │   │   │   ├── package.html
		│                   │   │   │   ├── remotevertex.java
		│                   │   │   │   ├── teststart.java
		│                   │   │   │   ├── vertexes.java
		│                   │   │   │   ├── vertexeswithui.java
		│                   │   │   │   └── vertex.java
		│                   │   │   ├── dparticles
		│                   │   │   │   ├── dparticle.java
		│                   │   │   │   ├── dparticles.java
		│                   │   │   │   ├── dparticleswithui.java
		│                   │   │   │   ├── icored.png
		│                   │   │   │   ├── index.html
		│                   │   │   │   ├── package.html
		│                   │   │   │   ├── remoteparticle.java
		│                   │   │   │   ├── teststart.java
		│                   │   │   │   └── tutorial3.html
		│                   │   │   ├── dparticlesthin
		│                   │   │   │   ├── dparticle.java
		│                   │   │   │   ├── dparticles.java
		│                   │   │   │   ├── dparticleswithui.java
		│                   │   │   │   ├── icored.png
		│                   │   │   │   ├── index.html
		│                   │   │   │   ├── package.html
		│                   │   │   │   ├── remoteparticle.java
		│                   │   │   │   ├── teststart.java
		│                   │   │   │   └── tutorial3.html
		│                   │   │   └── sociallydamagingbehavior
		│                   │   │       ├── analysis
		│                   │   │       │   ├── builddata.java
		│                   │   │       │   └── mainframe.java
		│                   │   │       ├── dbehaviour.java
		│                   │   │       ├── dhuman.java
		│                   │   │       ├── dishonest.java
		│                   │   │       ├── dsociallydamagingbehavior.java
		│                   │   │       ├── dsociallydamagingbehaviorwithui.java
		│                   │   │       ├── entryagent.java
		│                   │   │       ├── honest.java
		│                   │   │       ├── icon.png
		│                   │   │       ├── index.html
		│                   │   │       ├── pastdata.java
		│                   │   │       ├── remotehuman.java
		│                   │   │       └── teststart.java
		│                   │   ├── engine
		│                   │   │   ├── distributedagentfactory.java
		│                   │   │   ├── distributedmultischedule.java
		│                   │   │   ├── distributedstateconnectionjms.java
		│                   │   │   ├── distributedstateconnectionmpi.java
		│                   │   │   ├── distributedstate.java
		│                   │   │   ├── package.html
		│                   │   │   ├── remoteagent.java
		│                   │   │   ├── remoteagentstate.java
		│                   │   │   ├── remotepositionedagent.java
		│                   │   │   ├── remoteunpositionedagent.java
		│                   │   │   └── test
		│                   │   │       ├── distributedstateconnectionfake.java
		│                   │   │       └── fakeupdaterthreadforlistener.java
		│                   │   └── field
		│                   │       ├── celltype.java
		│                   │       ├── continuous
		│                   │       │   ├── dcontinuous2dfactory.java
		│                   │       │   ├── dcontinuous2d.java
		│                   │       │   ├── dcontinuous2dxy.java
		│                   │       │   ├── dcontinuous2dy.java
		│                   │       │   ├── loadbalanced
		│                   │       │   │   ├── dcontinuous2dxylb.java
		│                   │       │   │   ├── dcontinuous2dylb.java
		│                   │       │   │   └── package.html
		│                   │       │   ├── package.html
		│                   │       │   ├── region
		│                   │       │   │   ├── package.html
		│                   │       │   │   ├── regiondouble.java
		│                   │       │   │   └── regiondoublelb.java
		│                   │       │   └── thin
		│                   │       │       ├── dcontinuous2dthin.java
		│                   │       │       ├── dcontinuous2dxythin.java
		│                   │       │       ├── dcontinuous2dythin.java
		│                   │       │       └── package.html
		│                   │       ├── distributedfield2d.java
		│                   │       ├── distributedfield.java
		│                   │       ├── distributedfieldnetwork.java
		│                   │       ├── grid
		│                   │       │   ├── numeric
		│                   │       │   │   ├── ddoublegrid2dfactory.java
		│                   │       │   │   ├── ddoublegrid2d.java
		│                   │       │   │   ├── ddoublegrid2dxy.java
		│                   │       │   │   ├── ddoublegrid2dy.java
		│                   │       │   │   ├── dintgrid2dfactory.java
		│                   │       │   │   ├── dintgrid2d.java
		│                   │       │   │   ├── dintgrid2dxy.java
		│                   │       │   │   ├── dintgrid2dy.java
		│                   │       │   │   ├── loadbalanced
		│                   │       │   │   │   ├── ddoublegrid2dxylb.java
		│                   │       │   │   │   ├── ddoublegrid2dylb.java
		│                   │       │   │   │   ├── dintgrid2dxylb.java
		│                   │       │   │   │   ├── dintgrid2dylb.java
		│                   │       │   │   │   └── package.html
		│                   │       │   │   ├── package.html
		│                   │       │   │   ├── region
		│                   │       │   │   │   ├── package.html
		│                   │       │   │   │   ├── regiondoublenumeric.java
		│                   │       │   │   │   ├── regiondoublenumericlb.java
		│                   │       │   │   │   ├── regionintegernumeric.java
		│                   │       │   │   │   └── regionintegernumericlb.java
		│                   │       │   │   └── thin
		│                   │       │   │       ├── ddoublegrid2dthin.java
		│                   │       │   │       ├── ddoublegrid2dxythin.java
		│                   │       │   │       ├── ddoublegrid2dythin.java
		│                   │       │   │       ├── dintgrid2dthin.java
		│                   │       │   │       ├── dintgrid2dxythin.java
		│                   │       │   │       ├── dintgrid2dythin.java
		│                   │       │   │       └── package.html
		│                   │       │   ├── region
		│                   │       │   │   ├── package.html
		│                   │       │   │   ├── regioninteger.java
		│                   │       │   │   └── regionintegerlb.java
		│                   │       │   └── sparse
		│                   │       │       ├── dsparsegrid2dfactory.java
		│                   │       │       ├── dsparsegrid2d.java
		│                   │       │       ├── dsparsegrid2dxy.java
		│                   │       │       ├── dsparsegrid2dy.java
		│                   │       │       ├── loadbalanced
		│                   │       │       │   ├── dsparsegrid2dxylb.java
		│                   │       │       │   ├── dsparsegrid2dylb.java
		│                   │       │       │   └── package.html
		│                   │       │       ├── package.html
		│                   │       │       └── thin
		│                   │       │           ├── dsparsegrid2dthin.java
		│                   │       │           ├── dsparsegrid2dxythin.java
		│                   │       │           ├── dsparsegrid2dythin.java
		│                   │       │           └── package.html
		│                   │       ├── messagelistener.java
		│                   │       ├── network
		│                   │       │   ├── dnetworkfactory.java
		│                   │       │   ├── dnetwork.java
		│                   │       │   ├── kway
		│                   │       │   │   ├── algo
		│                   │       │   │   │   ├── interfaces
		│                   │       │   │   │   │   └── partitioningalgorithm.java
		│                   │       │   │   │   ├── jabeja
		│                   │       │   │   │   │   ├── intarrayconverter.java
		│                   │       │   │   │   │   ├── jabeja.java
		│                   │       │   │   │   │   ├── messagerelay.java
		│                   │       │   │   │   │   └── partitionanalysis.java
		│                   │       │   │   │   ├── kaffpa
		│                   │       │   │   │   │   ├── kaffpaeprocessbinding.java
		│                   │       │   │   │   │   └── kaffpaprocessbinding.java
		│                   │       │   │   │   ├── metis
		│                   │       │   │   │   │   ├── metisprocessbinding.java
		│                   │       │   │   │   │   └── metisrelaxedprocessbinding.java
		│                   │       │   │   │   └── random
		│                   │       │   │   │       └── random.java
		│                   │       │   │   ├── benchmark
		│                   │       │   │   │   └── algobenchmark.java
		│                   │       │   │   ├── graph
		│                   │       │   │   │   ├── edge.java
		│                   │       │   │   │   ├── graph.java
		│                   │       │   │   │   ├── superedge.java
		│                   │       │   │   │   ├── supervertex.java
		│                   │       │   │   │   ├── tools
		│                   │       │   │   │   │   ├── cleaner.java
		│                   │       │   │   │   │   ├── directtoindirect.java
		│                   │       │   │   │   │   ├── graphformatconverter.java
		│                   │       │   │   │   │   ├── metrics.java
		│                   │       │   │   │   │   ├── utility.java
		│                   │       │   │   │   │   └── vertexparser.java
		│                   │       │   │   │   └── vertex.java
		│                   │       │   │   ├── readme.md
		│                   │       │   │   └── util
		│                   │       │   │       ├── networkpartition.java
		│                   │       │   │       ├── partitioner.java
		│                   │       │   │       └── partitionmanager.java
		│                   │       │   ├── package.html
		│                   │       │   ├── partitioning
		│                   │       │   │   ├── algo
		│                   │       │   │   │   ├── dendogram
		│                   │       │   │   │   │   ├── dendrogramextractsubgraph.java
		│                   │       │   │   │   │   ├── dendrogramfixcommunity.java
		│                   │       │   │   │   │   ├── dnode.java
		│                   │       │   │   │   │   ├── dtree.java
		│                   │       │   │   │   │   ├── filetree.java
		│                   │       │   │   │   │   ├── louvain
		│                   │       │   │   │   │   │   └── louvain.java
		│                   │       │   │   │   │   └── util
		│                   │       │   │   │   │       ├── extractworker.java
		│                   │       │   │   │   │       ├── fixworker.java
		│                   │       │   │   │   │       ├── graphimporter.java
		│                   │       │   │   │   │       ├── graphmlimpoter.java
		│                   │       │   │   │   │       └── importexception.java
		│                   │       │   │   │   ├── random
		│                   │       │   │   │   │   └── randomkpart.java
		│                   │       │   │   │   └── ukwaypart
		│                   │       │   │   │       └── ukwaypartruntimeexec.java
		│                   │       │   │   ├── interfaces
		│                   │       │   │   │   ├── graphvertex.java
		│                   │       │   │   │   └── labelvertex.java
		│                   │       │   │   ├── partitioner.java
		│                   │       │   │   └── supergraphstats.java
		│                   │       │   └── region
		│                   │       │       ├── package.html
		│                   │       │       └── regionnetwork.java
		│                   │       ├── package.html
		│                   │       ├── support
		│                   │       │   ├── field2d
		│                   │       │   │   ├── distributedregioninterface.java
		│                   │       │   │   ├── distributedregion.java
		│                   │       │   │   ├── distributedregionnumeric.java
		│                   │       │   │   ├── entry.java
		│                   │       │   │   ├── entrynum.java
		│                   │       │   │   ├── loadbalanced
		│                   │       │   │   │   ├── distributedregionlb.java
		│                   │       │   │   │   ├── distributedregionnumericlb.java
		│                   │       │   │   │   ├── package.html
		│                   │       │   │   │   ├── updatepositiondoublefield.java
		│                   │       │   │   │   ├── updatepositiondoublenumeric.java
		│                   │       │   │   │   ├── updatepositionintegerfield.java
		│                   │       │   │   │   ├── updatepositionintegernumeric.java
		│                   │       │   │   │   └── updatepositioninterface.java
		│                   │       │   │   ├── package.html
		│                   │       │   │   ├── region
		│                   │       │   │   │   ├── package.html
		│                   │       │   │   │   ├── region.java
		│                   │       │   │   │   ├── regionmap.java
		│                   │       │   │   │   ├── regionmapnumeric.java
		│                   │       │   │   │   └── regionnumeric.java
		│                   │       │   │   └── updatemap.java
		│                   │       │   ├── globals
		│                   │       │   │   ├── globalinspectorhelper.java
		│                   │       │   │   ├── globalparametershelper.java
		│                   │       │   │   └── package.html
		│                   │       │   ├── loadbalancing
		│                   │       │   │   ├── loadbalancingdoublefield.java
		│                   │       │   │   ├── loadbalancingdoublenumeric.java
		│                   │       │   │   ├── loadbalancingintegerfield.java
		│                   │       │   │   ├── loadbalancingintegernumeric.java
		│                   │       │   │   ├── loadbalancinginterface.java
		│                   │       │   │   ├── mycelldoublefield.java
		│                   │       │   │   ├── mycelldoublenumeric.java
		│                   │       │   │   ├── mycellintegerfield.java
		│                   │       │   │   ├── mycellintegernumeric.java
		│                   │       │   │   ├── mycellinterface.java
		│                   │       │   │   ├── package.html
		│                   │       │   │   ├── splitteddoublefield.java
		│                   │       │   │   ├── splitteddoublenumeric.java
		│                   │       │   │   ├── splittedintegernumeric.java
		│                   │       │   │   └── splittedintfield.java
		│                   │       │   └── network
		│                   │       │       ├── basicvertex.java
		│                   │       │       ├── dnetworkjmsmessagelistener.java
		│                   │       │       ├── dnetworkregion.java
		│                   │       │       ├── edgewrapper.java
		│                   │       │       ├── graphsubscribersedgelist.java
		│                   │       │       ├── importserializedsimplegraph.java
		│                   │       │       ├── package.html
		│                   │       │       ├── updatenetworkmap.java
		│                   │       │       └── updaterthreadjmsfornetworklistener.java
		│                   │       ├── traceablefield.java
		│                   │       ├── updatecell.java
		│                   │       └── updaterthreadforlistener.java
		│                   ├── tools
		│                   │   ├── batch
		│                   │   │   ├── batchexecutor.java
		│                   │   │   ├── batchlistener.java
		│                   │   │   ├── batchwizard.java
		│                   │   │   ├── data
		│                   │   │   │   ├── batch.java
		│                   │   │   │   ├── entryparam.java
		│                   │   │   │   ├── entryworkerscore.java
		│                   │   │   │   ├── generalparam.java
		│                   │   │   │   ├── package.html
		│                   │   │   │   ├── paramdistributionexponential.java
		│                   │   │   │   ├── paramdistribution.java
		│                   │   │   │   ├── paramdistributionnormal.java
		│                   │   │   │   ├── paramdistributionuniform.java
		│                   │   │   │   ├── paramfixed.java
		│                   │   │   │   ├── param.java
		│                   │   │   │   ├── paramlist.java
		│                   │   │   │   ├── paramrange.java
		│                   │   │   │   ├── scorecomparator.java
		│                   │   │   │   └── testparam.java
		│                   │   │   └── package.html
		│                   │   └── launcher
		│                   │       ├── forker.java
		│                   │       ├── launcher.java
		│                   │       ├── package.html
		│                   │       └── ui
		│                   │           ├── package.html
		│                   │           └── wizard.java
		│                   └── util
		│                       ├── connection
		│                       │   ├── address.java
		│                       │   ├── connection.java
		│                       │   ├── connectiontype.java
		│                       │   ├── jms
		│                       │   │   ├── activemq
		│                       │   │   │   ├── connectionnfieldswithactivemqapi.java
		│                       │   │   │   ├── mymessagelistener.java
		│                       │   │   │   └── package.html
		│                       │   │   ├── activemqmanager.java
		│                       │   │   ├── beaconmessage.java
		│                       │   │   ├── beaconmessagelistener.java
		│                       │   │   ├── connectionjms.java
		│                       │   │   ├── dmasonconnectionfactory.java
		│                       │   │   └── package.html
		│                       │   ├── mpi
		│                       │   │   ├── connectionmpi.java
		│                       │   │   ├── dnetworkmpimessagelistener.java
		│                       │   │   ├── mpimessagelistener.java
		│                       │   │   ├── mpinfieldslisteners.java
		│                       │   │   ├── mpitopic.java
		│                       │   │   ├── mpitopicmessage.java
		│                       │   │   ├── openmpi
		│                       │   │   │   ├── bcast
		│                       │   │   │   │   ├── connectionnfieldswithbcastmpibyte.java
		│                       │   │   │   │   └── connectionnfieldswithbcastmpi_deprecated.java
		│                       │   │   │   ├── gather
		│                       │   │   │   │   ├── connectionnfieldswithgathermpibyte.java
		│                       │   │   │   │   └── connectionnfieldswithgathermpi_deprecated.java
		│                       │   │   │   ├── parallel
		│                       │   │   │   │   ├── connectionnfieldswithparallelschedulermpi.java
		│                       │   │   │   │   ├── connectionnfieldswiththreadsmpi.java
		│                       │   │   │   │   └── scheduler
		│                       │   │   │   │       ├── parallelscheduler.java
		│                       │   │   │   │       ├── round.java
		│                       │   │   │   │       └── tuple.java
		│                       │   │   │   └── test
		│                       │   │   │       └── testthreadmultiple.java
		│                       │   │   ├── updaterthreadformpinetworklistener.java
		│                       │   │   └── utilconnectionmpi.java
		│                       │   ├── myhashmap.java
		│                       │   ├── package.html
		│                       │   ├── socket
		│                       │   │   ├── connectionwithsocket.java
		│                       │   │   ├── package.html
		│                       │   │   ├── proxyconnection.java
		│                       │   │   ├── pubsubmessage.java
		│                       │   │   └── serverpublishsubscribe.java
		│                       │   └── testconnection
		│                       │       ├── virtualconnection.java
		│                       │       ├── virtualconnectionnfieldswithvirtualjms.java
		│                       │       └── virtualmessagelistener.java
		│                       ├── distributedproperties.java
		│                       ├── package.html
		│                       ├── stdrandom.java
		│                       ├── systemmanagement
		│                       │   ├── digestalgorithm.java
		│                       │   ├── garbagecollector
		│                       │   │   ├── package.html
		│                       │   │   ├── server.java
		│                       │   │   └── start.java
		│                       │   ├── globals
		│                       │   │   ├── messagelistenerglobalsdata.java
		│                       │   │   ├── messagelistenerglobals.java
		│                       │   │   ├── package.html
		│                       │   │   ├── reducer.java
		│                       │   │   ├── updaterthreadforglobalsdatalistener.java
		│                       │   │   ├── updaterthreadforglobalslistener.java
		│                       │   │   └── util
		│                       │   │       ├── package.html
		│                       │   │       └── updateglobalvaratstep.java
		│                       │   ├── inspection
		│                       │   │   ├── distributedinspector.java
		│                       │   │   ├── inspectableschedule.java
		│                       │   │   ├── inspectablestate.java
		│                       │   │   └── package.html
		│                       │   ├── jarclassloader.java
		│                       │   ├── master
		│                       │   │   ├── entryval.java
		│                       │   │   ├── masterdaemonlistener.java
		│                       │   │   ├── masterdaemonstarter.java
		│                       │   │   ├── modelpanel.java
		│                       │   │   ├── package.html
		│                       │   │   └── thrower
		│                       │   │       ├── dmasonmaster.java
		│                       │   │       └── testmaster.java
		│                       │   ├── package.html
		│                       │   ├── release.java
		│                       │   ├── transferlistener.java
		│                       │   ├── worker
		│                       │   │   ├── digester.java
		│                       │   │   ├── package.html
		│                       │   │   ├── peerdaemonlistener.java
		│                       │   │   ├── peerdaemonstarter.java
		│                       │   │   ├── peerstatusinfo.java
		│                       │   │   ├── startupdata.java
		│                       │   │   ├── startworkerinterface.java
		│                       │   │   ├── systemmanager.java
		│                       │   │   ├── thrower
		│                       │   │   │   ├── dmasonworker.java
		│                       │   │   │   ├── dmasonworkerwithgui.java
		│                       │   │   │   └── testworker.java
		│                       │   │   ├── updatedata.java
		│                       │   │   ├── updater.java
		│                       │   │   ├── worker.java
		│                       │   │   └── workerupdater.java
		│                       │   └── wrapper
		│                       │       ├── activemq
		│                       │       │   ├── launcher
		│                       │       │   │   ├── activemqwrapper.java
		│                       │       │   │   └── package.html
		│                       │       │   └── rmi
		│                       │       │       ├── command.java
		│                       │       │       └── package.html
		│                       │       └── data
		│                       │           ├── beaconmessage.java
		│                       │           ├── broadcasttask.java
		│                       │           └── package.html
		│                       ├── trigger
		│                       │   ├── package.html
		│                       │   ├── triggerconnection.java
		│                       │   ├── trigger.java
		│                       │   └── triggerlistener.java
		│                       ├── util.java
		│                       └── visualization
		│                           ├── globalviewer
		│                           │   ├── display.java
		│                           │   ├── globalviewer.java
		│                           │   ├── package.html
		│                           │   ├── remotesnap.java
		│                           │   ├── simcomboentry.java
		│                           │   ├── testglobalviewer.java
		│                           │   ├── threadvisualizationcellmessagelistener.java
		│                           │   ├── threadvisualizationmessagelistener.java
		│                           │   ├── viewermonitor.java
		│                           │   ├── visualizationcellmessagelistener.java
		│                           │   ├── visualizationmessagelistener.java
		│                           │   └── visualizationupdatemap.java
		│                           ├── sim
		│                           │   └── app
		│                           │       ├── dantsforage
		│                           │       │   ├── antsforagewithuizoom.java
		│                           │       │   ├── antsforagezoom.java
		│                           │       │   ├── dantsagentupdate.java
		│                           │       │   ├── package.html
		│                           │       │   └── testzoomants.java
		│                           │       ├── dflockers
		│                           │       │   ├── dflockerupdate.java
		│                           │       │   ├── flockersview.java
		│                           │       │   ├── flockerswithuiview.java
		│                           │       │   ├── package.html
		│                           │       │   └── testzoomflockers.java
		│                           │       └── dparticles
		│                           │           ├── dparticlesagentupdate.java
		│                           │           ├── package.html
		│                           │           ├── testtutorial3.java
		│                           │           ├── tutorial3view.java
		│                           │           └── tutorial3viewwithui.java
		│                           └── zoomviewerapp
		│                               ├── closezoomapplistener.java
		│                               ├── consolezoom.java
		│                               ├── package.html
		│                               ├── threadzoomcellmessagelistener.java
		│                               ├── threadzoomincellmessagelistener.java
		│                               ├── updater.java
		│                               ├── zoomarraylist.java
		│                               ├── zoomcellmessagelistener.java
		│                               ├── zoomincellmessagelistener.java
		│                               └── zoomviewer.java
		└── test
		    ├── java
		    │   └── it
		    │       └── isislab
		    │           └── dmason
		    │               └── test
		    │                   ├── sim
		    │                   │   ├── app
		    │                   │   │   └── dflockers
		    │                   │   │       ├── dflocker.java
		    │                   │   │       ├── dflockers.java
		    │                   │   │       ├── dflockerswithui.java
		    │                   │   │       ├── icon.png
		    │                   │   │       ├── icored.png
		    │                   │   │       ├── index.html
		    │                   │   │       ├── package.html
		    │                   │   │       ├── remoteflock.java
		    │                   │   │       └── testdflockers.java
		    │                   │   ├── engine
		    │                   │   │   └── distributedstateconnectionjmstester.java
		    │                   │   └── field
		    │                   │       ├── celltypetester.java
		    │                   │       ├── continuous
		    │                   │       │   ├── dcontinuous2dfactorytester.java
		    │                   │       │   ├── dcontinuous2dxytester.java
		    │                   │       │   ├── loadbalanced
		    │                   │       │   │   └── dcontinuous2dxylbtester.java
		    │                   │       │   ├── region
		    │                   │       │   │   ├── regiondoublelbtester.java
		    │                   │       │   │   └── regiondoubletester.java
		    │                   │       │   └── thin
		    │                   │       │       └── dcontinuous2dxythintester.java
		    │                   │       ├── grid
		    │                   │       │   ├── numeric
		    │                   │       │   │   ├── ddoublegrid2dfactorytester.java
		    │                   │       │   │   ├── ddoublegrid2dxytester.java
		    │                   │       │   │   ├── dintgrid2dfactorytester.java
		    │                   │       │   │   ├── dintgrid2dxytester.java
		    │                   │       │   │   ├── loadbalanced
		    │                   │       │   │   │   ├── ddoublegrid2dxylbtester.java
		    │                   │       │   │   │   └── dintgrid2dxylbtester.java
		    │                   │       │   │   ├── region
		    │                   │       │   │   │   ├── regiondoublenumerictester.java
		    │                   │       │   │   │   └── regionintegernumerictester.java
		    │                   │       │   │   └── thin
		    │                   │       │   │       ├── ddoublegrid2dxythintester.java
		    │                   │       │   │       └── dintgrid2dxythintester.java
		    │                   │       │   ├── region
		    │                   │       │   │   ├── regionintegerlbtester.java
		    │                   │       │   │   └── regionintegertester.java
		    │                   │       │   └── sparse
		    │                   │       │       ├── dsparsegrid2dxytester.java
		    │                   │       │       ├── loadbalanced
		    │                   │       │       │   └── dsparsegrid2dxylbtester.java
		    │                   │       │       └── thin
		    │                   │       │           └── dsparsegrid2dxythintester.java
		    │                   │       ├── support
		    │                   │       │   └── field2d
		    │                   │       │       └── updatemaptester.java
		    │                   │       └── updatecelltester.java
		    │                   └── testsuite
		    │                       └── testsuite.java
		    └── readme.md
         
### 2. New package kway	
Add new package kway. (07/09/2015)

		kway
	    ├── algo
	    │   ├── interfaces
	    │   │   └── PartitioningAlgorithm.java
	    │   ├── jabeja
	    │   │   ├── IntArrayConverter.java
	    │   │   ├── JabeJa.java
	    │   │   ├── MessageRelay.java
	    │   │   └── PartitionAnalysis.java
	    │   ├── kaffpa
	    │   │   ├── KaffpaEProcessBinding.java
	    │   │   └── KaffpaProcessBinding.java
	    │   ├── metis
	    │   │   ├── MetisProcessBinding.java
	    │   │   └── MetisRelaxedProcessBinding.java
	    │   └── random
	    │       └── Random.java
	    ├── benchmark
	    │   └── AlgoBenchmark.java
	    ├── graph
	    │   ├── Edge.java
	    │   ├── Graph.java
	    │   ├── SuperEdge.java
	    │   ├── SuperVertex.java
	    │   ├── tools
	    │   │   ├── Cleaner.java
	    │   │   ├── DirectToIndirect.java
	    │   │   ├── GraphFormatConverter.java
	    │   │   ├── Metrics.java
	    │   │   ├── Utility.java
	    │   │   └── VertexParser.java
	    │   └── Vertex.java
	    ├── README.md
	    └── util
	        ├── NetworkPartition.java
	        ├── Partitioner.java
	        └── PartitionManager.java
		

### System Management

- Realization of a Web User interface for managing and submitting simulations on DMASON  
- Rebuild the comunication layer between Master and Workers 

### Documentation

- About DMASON
- how to build source from Eclipse
- How to run DMASON from Eclipse or Jar file.
- How to run DMASON on cluster 
- Simulation section: here are described the model and the main features useful for massive tests for Flockers,Ant foraging, Particle and SIR (susceptible,infected and recovered) simulations
- From MASON to DMASON: guide to how to make a distribute simulation, with a code description example (from Particle to D-Particle) 
- System Management: Description of Web UI, all features are described in the section  
- 
- Benchmarks: a strong scalability testing was made. In this section is described the cluster configuration and the tests made.
- Testing Simulation: expones what features must be tested after made changing

### Site
- New D-MASON site created with Polymer web components (Google material style) on https://isislab-unisa.github.io/dmason/
- All documentation has been forwarded to GitHub wiki

### Project
- Javadoc autogenerate while compiling the source with maven
- A Readme file has been created for each simulation model
- A configuration file has been created to set ActivemQ ip and port  

## TODO LIST
- Workers reconnection (beta version has been created) 
- Simulation Viewer for uniform field partitioning (beta version has been created) 
- Include communication with MPI in System Management 
- Ultimate JUnit Testing
- Distributed 3D Fields  



For more details see the [README.md](https://github.com/isislab-unisa/dmason/blob/master/src/main/java/it/isislab/dmason/sim/field/network/kway/README.md) file.           