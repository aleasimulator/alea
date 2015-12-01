/*
 Copyright (c) 2015 Simon Toth (kontakt@simontoth.cz)

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

import alea.dynamic.JobBatchDynamic;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import xklusac.environment.ExperimentSetup;
import xklusac.extensions.Input;

/**
 * Reader for the job batch format
 *
 * Used in dynamic workload generation support.
 *
 * @author Simon Toth (kontakt@simontoth.cz)
 */
public class BatchReader {

    Input r = new Input();
    BufferedReader br = null;

    public BatchReader(String name) {
        br = r.openFile(new File(ExperimentSetup.data_sets + "/" + name));
    }

    private JobBatchDynamic read_batch() {
        String line;

        try {
            line = br.readLine();
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
            return null;
        }

        if (line == null) {
            return null;
        }

        Scanner sc = new Scanner(line).useDelimiter("\t");

        int session_id = sc.nextInt();
        int batch_id = sc.nextInt();
        int start_time = sc.nextInt();
        int end_time = sc.nextInt();

        ArrayList<AbstractMap.SimpleEntry<Integer, Integer>> after_completion
                = new ArrayList<AbstractMap.SimpleEntry<Integer, Integer>>();
        ArrayList<AbstractMap.SimpleEntry<Integer, Integer>> after_arrival
                = new ArrayList<AbstractMap.SimpleEntry<Integer, Integer>>();
        ArrayList<String> job_list = new ArrayList<String>();

        while (true) {
            int after = "after_completion:".length();
            int follows = "after_arrival:".length();
            int jobs = "job:".length();

            String cvalue;
            try {
                cvalue = sc.next();
            } catch (java.util.NoSuchElementException e) {
                break;
            }

            if (cvalue.length() > after && cvalue.startsWith("after_completion:")) {

                String value = cvalue.substring(after, cvalue.length());
                String ids[] = value.split("\\|");
                after_completion.add(new AbstractMap.SimpleEntry<Integer, Integer>(Integer.parseInt(ids[0]), Integer.parseInt(ids[1])));

            } else if (cvalue.length() > follows && cvalue.startsWith("after_arrival:")) {

                String value = cvalue.substring(follows, cvalue.length());
                String ids[] = value.split("\\|");
                after_arrival.add(new AbstractMap.SimpleEntry<Integer, Integer>(Integer.parseInt(ids[0]), Integer.parseInt(ids[1])));

            } else if (cvalue.length() > jobs && cvalue.startsWith("job:")) {

                String value = cvalue.substring(jobs, cvalue.length());
                String items[] = value.split("\\.", 2);
                job_list.add(items[0]);
            }
        }

        return new JobBatchDynamic(session_id, batch_id, start_time, end_time, job_list, after_arrival, after_completion);
    }

    public List<JobBatchDynamic> read_batches() {
        List<JobBatchDynamic> batches = new ArrayList<JobBatchDynamic>();
        JobBatchDynamic current_batch;
        while ((current_batch = read_batch()) != null) {
            batches.add(current_batch);
        }

        return batches;
    }
}
