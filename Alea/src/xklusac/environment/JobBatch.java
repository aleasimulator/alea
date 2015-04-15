/*
Copyright (c) 2015 Šimon Tóth (toth@fi.muni.cz)

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

package xklusac.environment;

import java.util.List;

/** Interface for job batches
 * 
 * Job batches represent sets of jobs that have arrived in a single user session
 * and interset in their arrival-completion.
 * 
 * @author Simon Toth
 */
public interface JobBatch {
    
    /** Get next job in this batch.
     * 
     * @return Gridlet or null if there are no more jobs in this batch
     */
    public ComplexGridlet getNextJob();
    
    /** Returns whether all batch dependencies are fullfilled.
     * 
     * @return true if processing of this batch can start, false otherwise
     */
    public Boolean canStart();
    
    /** Returns whether the batch has arrived
     * 
     * Batch is considered arrived once the first job has arrived.
     * @return 
     */
    public Boolean hasArrived();
    
    /** Returns whether the batch has fully completed
     * 
     * Batch is considered completed once all jobs in the batch have completed.
     * @return 
     */
    public Boolean hasCompleted();
    
    /** Returns which batches should arrive before this one can process.
     * 
     * @return List of dependent batches.
     */
    public List<JobBatch> arriveBefore();
    
    /** Returns which batches should complete before this one can process.
     * 
     * @return List of dependent batches.
     */    
    public List<JobBatch> completeBefore();
    
    /** Arrival time of the first job in the batch.
     * 
     * @return 
     */
    public double firstJobArrival();
    
    /** Arrival time of the last job in the batch
     * 
     * @return 
     */
    public double lastJobArrival();
    
    /** Completion time of the last job in the batch
     * 
     * @return 
     */
    public double lastJobCompletion();
    
    /** Mark a job in this batch as completed
     * 
     * @param id ID of the completed job
     */
    public void notifyJobCompletion(String id);
}
