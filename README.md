![Logo](https://raw.githubusercontent.com/aleasimulator/alea/master/AleaWebConfiguration/web/images/logo1.png)
### GridSim based Job Scheduling Simulator
This work concentrates on the design of a system intended for study of advanced scheduling techniques for planning various types of jobs in Grid environment. The solution is able to deal with common problems of job scheduling in Clusters and Grids, like heterogeneity of jobs and resources, and dynamic runtime changes such as arrival of new jobs.

Alea Simulator is based on the latest [GridSim 5](http://www.cloudbus.org/gridsim/) simulation toolkit which we extended to provide a simulation environment that supports simulation of varying job scheduling problems. To demonstrate the features of the Alea environment, we implemented an experimental centralised job scheduler which uses advanced scheduling techniques for schedule generation. By now local search based algorithms and some policies were tested as well as "classical" queue-based algorithms such as FCFS or Easy Backfilling.

The scheduler is capable to handle dynamic situation when jobs appear in the system during simulation. In this case generated schedule is changing through time as some jobs are already finished while the new ones are arriving.

The sources are stored in the Netbeans IDE project format. You can download Netbeans IDE for free from http://www.netbeans.org.
For the proper function you have to include simjava.jar (re-modified in Jan 2009) which is [here](http://www.fi.muni.cz/~xklusac/alea/download/simjava.jar).
You will also need gridsim.jar which you can download [here](http://www.gridbus.org/gridsim/).

The data sets are available at http://www.fi.muni.cz/~xklusac/workload and http://www.cs.huji.ac.il/labs/parallel/workload/logs.html. Sample data sets are provided within the distribution but only serve for demonstration purposes. Alea uses its own machine-description format, storing it in a file with .machines filename extension. The file format specifies one computer cluster per line, attributes are separated by TAB space. One line has following attributes:

<pre><code>cluster_id   cluster_name  number_of_nodes   CPUs_per_node  CPU_speed   RAM_per_node_in_KB</code></pre>
So, in the following example we have two clusters (zewura/zegox), having 20/48 nodes, each having 80/12 CPUs per node (with default speed 1) and approx. 500/90 GB of RAM per each node:
<pre><code>0	zewura	20	80	1	529426432
1	zegox	48	12	1	94035968 </code></pre>
For further information, please refer to http://www.fi.muni.cz/~xklusac/alea/index.html.

##### Software licence:
This software is provided as is, free of charge under the terms of the LGPL licence. 

##### Important
When using Alea in your paper or presentation, please use the following citations as an acknowledgement. Thank you!
- Dalibor Klusáček, Gabriela Podolníková and Šimon Tóth. Complex Job Scheduling Simulations with Alea 4. In Proceedings of the 9th EAI International Conference on Simulation Tools and Techniques, pages 124-129, 2016. 
- Dalibor Klusáček and Hana Rudová. Alea 2 - Job Scheduling Simulator. In proceedings of the 3rd International ICST Conference on Simulation Tools and Techniques (SIMUTools 2010), ICST, 2010. [download](http://www.fi.muni.cz/~xklusac/pub/alea2.pdf)
