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

import alea.dynamic.JobSessionDynamic;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import xklusac.environment.ExperimentSetup;
import xklusac.extensions.Input;

/**
 * Reader for the job session format
 *
 * Used in dynamic workload generation support.
 *
 * @author Simon Toth (kontakt@simontoth.cz)
 */
public class SessionReader {

    Input r = new Input();
    BufferedReader br = null;

    public SessionReader(String name) {
        br = r.openFile(new File(ExperimentSetup.data_sets + "/" + name));
    }

    private JobSessionDynamic read_session() {
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

        String values[] = line.split("\t");

        int session_id = Integer.parseInt(values[0]);
        int first_arrival = Integer.parseInt(values[1]);
        int last_arrival = Integer.parseInt(values[2]);
        int last_completion = Integer.parseInt(values[3]);

        return new JobSessionDynamic(session_id, first_arrival, last_arrival, last_completion);
    }

    public List<JobSessionDynamic> read_sessions() {
        List<JobSessionDynamic> sessions = new ArrayList<JobSessionDynamic>();
        JobSessionDynamic current_session;
        while ((current_session = read_session()) != null) {
            sessions.add(current_session);
        }

        return sessions;
    }

}
