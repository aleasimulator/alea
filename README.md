##ALEA 3 GridSim based Grid Scheduling Simulator
This work concentrates on the design of a system intended for study of advanced scheduling techniques for planning various types of jobs in Grid environment. The solution is able to deal with common problems of job scheduling in Grids like heterogeneity of jobs and resources, and dynamic runtime changes such as arrival of new jobs.

Alea Simulator is based on the latest [GridSim 5](http://www.cloudbus.org/gridsim/) simulation toolkit which we extended to provide a simulation environment that supports simulation of varying Grid scheduling problems. To demonstrate the features of the Alea environment, we implemented an experimental centralised Grid scheduler which uses advanced scheduling techniques for schedule generation. By now local search based algorithms and some policies were tested as well as "classical" queue-based algorithms such as FCFS or Easy Backfilling.

The scheduler is capable to handle dynamic situation when jobs appear in the system during simulation. In this case generated schedule is changing through time as some jobs are already finished while the new ones are arriving.

The sources are stored in the Netbeans IDE project format. You can download Netbeans IDE for free from http://www.netbeans.org.
For the proper function you have to include simjava.jar (re-modified in Jan 2009) which is [here](http://www.fi.muni.cz/~xklusac/alea/download/simjava.jar).
You will also need gridsim.jar which you can download [here](http://www.gridbus.org/gridsim/).

The data sets are available at: http://www.cs.huji.ac.il/labs/parallel/workload/logs.html.

For further information, please refer to http://www.fi.muni.cz/~xklusac/alea/index.html.

#####Software licence:
This software is the result of the research intent No. 0021622419 (Ministry of Education, Youth and Sports of the Czech Republic) and the grant No. 201/07/0205 (Grant Agency of the Czech Republic) and this result is consistent with the expected objectives of these projects. The owner of the result is Masaryk University, a public high school, ID: 00216224. Masaryk University allows other companies and individuals to use this software free of charge and without territorial restrictions under the terms of the LGPL licence. 
This permission is granted for the duration of property rights. This software is not subject to special information treatment according to Act No. 412/2005 Coll., as amended. In case that a person who will use the software under this license offer violates the license terms, the permission to use the software terminates.

#####Important
When using Alea in your paper or presentation, please use the following citation as an acknowledgement. Thank you!

Dalibor Klusáček and Hana Rudová. Alea 2 - Job Scheduling Simulator. In proceedings of the 3rd International ICST Conference on Simulation Tools and Techniques (SIMUTools 2010), ICST, 2010.
[download](http://www.fi.muni.cz/~xklusac/pub/alea2.pdf)
