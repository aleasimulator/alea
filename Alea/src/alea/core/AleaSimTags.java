/*
Copyright (c) 2014 Šimon Tóth

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all 
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
SOFTWARE.
*/

package alea.core;

/**
 * Simulation Tags specific to Alea Simulator
 *
 * For the remaining Tags see GridSimTags.
 *
 * @author Šimon Tóth (kontakt@simontoth.cz)
 */
public class AleaSimTags {
     private static final int TAG_BASE = 65536;

     /** Tag for submision done event */
     public static final int SUBMISION_DONE = TAG_BASE+1;

     /** Tag for gridlet sent event */
     public static final int GRIDLET_SENT = TAG_BASE+101;
     /** Tag for gridlet started event */
     public static final int GRIDLET_STARTED = TAG_BASE+102;
     /** Tag for gridlet information event */
     public static final int GRIDLET_INFO = TAG_BASE+103;

     /** Tag for internal wake event */
     public static final int EVENT_WAKE = TAG_BASE+901;
     /** Tag for internal optimize wake event */
     public static final int EVENT_OPTIMIZE = TAG_BASE+902;
     /** Tag for interal scheduler wake event */
     public static final int EVENT_SCHEDULE = TAG_BASE+903;
     /** Tag depicting the start of a machine failure */
     public static final int FAILURE_START = TAG_BASE+501;
     public static final int FAILURE_FINISHED = TAG_BASE+502;
     public static final int FAILURE_RESTART = TAG_BASE+503;
     public static final int FAILURE_INFO = TAG_BASE+504;
     public static final int FAILURE_MACHINE = TAG_BASE+505;
     public static final int FAILURE_RESTART2 = TAG_BASE+506;
     
     public static final int LOG_SCHEDULER = TAG_BASE+801;
     
     public static final int FAIRSHARE_WEIGHT_DECAY = TAG_BASE+701;
     public static final int FAIRSHARE_UPDATE = TAG_BASE+702;
     
     public static final int SCHEDULER_OPTIMIZE_ONDEMAND = TAG_BASE+601;
     public static final int SCHEDULER_COLLECT = TAG_BASE+602;
}
