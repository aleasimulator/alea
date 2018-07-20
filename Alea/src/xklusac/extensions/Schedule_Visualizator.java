/*
 * Schedule Visualizator
 */
package xklusac.extensions;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;
import xklusac.environment.DirectoryLevel;
import xklusac.environment.ExperimentSetup;

/**
 * Class Schedule Visualizator<p>
 * It is used to create a JPanel where graphs are drawn. What is drawn is driven
 * by the paintComponent(Graphics g) method.<p>
 * If you want to change the graphical output, rewrite this method.
 *
 * @author Dalibor Klusacek
 */
public class Schedule_Visualizator extends JPanel implements Runnable {

    BufferedImage image;
    int scale = 1;
    Thread thread;
    boolean animating = false;
    boolean increasing = true;
    public final static Color brown = new Color(116, 74, 8);
    public final static Color green2 = new Color(123, 255, 5);
    public final static Color red2 = new Color(136, 24, 2);
    public final static Color blue2 = new Color(14, 219, 232);
    public final static Color yellow2 = new Color(196, 176, 12);
    public final static Color orange2 = new Color(203, 76, 0);
    public final static Color gray2 = new Color(120, 152, 118);
    public final static Color brown2 = new Color(148, 114, 70);
    public final static Color blue3 = new Color(1, 15, 16);
    Color colors[] = {
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2,
        Color.red, Color.BLUE, Color.green, Color.yellow, Color.gray,
        Color.ORANGE, Color.cyan, Color.MAGENTA, Color.pink, Color.darkGray, Color.black,
        brown, green2, red2, blue2, yellow2, orange2, gray2, blue3, brown2
    };

    public int jobh = 6;
    public int jobw = 9;
    public boolean draw_age = false;
    public boolean draw_user = false;

    long time = 0;
    boolean usage = false;
    boolean day_usage = false;
    boolean full_usage = false;
    boolean full_wait = false;
    boolean full_run = false;
    LinkedList<Integer> days = new LinkedList();
    LinkedList<Double> util = new LinkedList();
    LinkedList<Double> cl_util = new LinkedList();
    LinkedList<Double> cl_status = new LinkedList();
    LinkedList<Double> cl_util_h = new LinkedList();
    LinkedList<Double> day_util = new LinkedList();
    LinkedList<String> cl_names = new LinkedList();
    LinkedList<Integer> running = new LinkedList();
    LinkedList<Integer> cl_CPUs = new LinkedList();
    LinkedList<Integer> waiting = new LinkedList();
    LinkedList<Integer> requested = new LinkedList();
    LinkedList<Integer> used = new LinkedList();
    LinkedList<Integer> hours = new LinkedList();
    LinkedList<Integer> availCPUs = new LinkedList();
    ArrayList<SchedulingEvent> job_schedule = new ArrayList();
    ArrayList[] schedules = null;

    private static LinkedList<Schedule_Visualizator> ia;

    boolean draw_schedule = false;
    int cl_count = 0;
    int totCPUs = 0;
    static Random rnd = new Random(1080);

    /**
     * Constructor. Creates one JPanel inside some JFrame. JPanel will be used
     * to paint on graphs.
     *
     */
    public Schedule_Visualizator() {
    }

    /**
     * Rewrite this method to obtain new graphical output. Get inspired by the
     * present code.
     *
     */
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        int w = getWidth();
        int h = getHeight();
        g2.setColor(Color.black);

        // draw schedule
        if (draw_schedule) {
            //draw y axis

            int xpos_start = 150;
            int ypos = 10;
            int schedule_height = 0;
            // go over clusters
            for (int c = 0; c < cl_names.size(); c++) {
                // draw cluster name
                g2.setColor(colors[c]);
                g2.drawString(cl_names.get(c), 5, ypos);

                g2.setColor(Color.black);
                g2.drawLine(xpos_start - 2, ypos, w, ypos);

                // draw cluster CPUs
                for (int p = 0; p < cl_CPUs.get(c); p++) {
                    g2.setColor(colors[c]);
                    g2.drawLine(xpos_start - 2, ypos, xpos_start, ypos);
                    if (p % 4 == 0) {
                        g2.drawLine(xpos_start - 4, ypos, xpos_start, ypos);
                        g2.drawString(p + "", xpos_start - 27, ypos + 5);
                    }
                    //g2.drawRect(xpos_start+s, ypos, jobw, jobh);
                    ypos += jobh;
                }
                g2.setColor(Color.black);
                g2.drawLine(xpos_start - 2, ypos, w, ypos);
            }
            g2.drawLine(xpos_start, 10, xpos_start, ypos);

            //draw x axis
            g2.drawLine(xpos_start, ypos, w, ypos);
            int x_ax = xpos_start;
            // draw ticks on x axis
            while (x_ax < w) {
                g2.drawLine(x_ax, ypos, x_ax, ypos + 3);
                x_ax += jobw;
            }
            schedule_height = ypos;

            // draw schedule
            ypos = 10;
            // 
            for (int c = 0; c < cl_names.size(); c++) {
                job_schedule.addAll(schedules[c]);
            }
            Collections.sort(job_schedule, new EndTimeComparator());
            long previous_time = 0;
            int tick = -1;

            // find minimum and max expected waiting time
            double min_wait = Double.MAX_VALUE;
            double max_wait = 0.0;
            if (draw_age) {
                for (int j = 0; j < job_schedule.size(); j++) {
                    SchedulingEvent se = job_schedule.get(j);
                    if (!se.isStart()) {
                        double wait = se.getGi().getExpectedWaitTime();
                        if (min_wait > wait) {
                            min_wait = wait;
                        }
                        if (max_wait < wait) {
                            max_wait = wait;
                        }
                    }
                }
            }

            //find the correct location for the first job            
            for (int j = 0; j < job_schedule.size(); j++) {
                SchedulingEvent se = job_schedule.get(j);

                if (se.getSch_time() > previous_time) {
                    tick++;
                    previous_time = se.getSch_time();
                }
                se.setTick(tick);

                // we only care about job ends - redrawing the job backward when we have all the info (just like in PBS logs) 
                if (!se.isStart()) {
                    //find corresponding start event and print
                    SchedulingEvent jobstart = se.getStart_event();

                    // draw one job with random color or by its "age" color
                    Random rnd = new Random(se.getGi().getID() * 1024 * 1981);
                    int r = rnd.nextInt(255);
                    int g = rnd.nextInt(255);
                    int b = rnd.nextInt(255);
                    if (draw_age) {
                        double distance = max_wait - min_wait;
                        double position = jobstart.getGi().getExpectedWaitTime() - min_wait;
                        position = position / distance;
                        int rgb_position = Long.valueOf(Math.round(position * 255)).intValue();
                        r = 255;
                        g = Math.min(255, 255 - rgb_position);
                        g = Math.max(0, g);
                        b = 0;
                    }
                    if (draw_user) {
                        Random rnd_user = new Random(se.getGi().getUser().hashCode()* 1024 * 1981);
                        r = rnd_user.nextInt(256);
                        g = rnd_user.nextInt(256);
                        b = rnd_user.nextInt(256);
                    }

                    g2.setColor(new Color(r, g, b));
                    List<Integer> cpus = getListCopy(se.getGi().getPlannedPEs());
                    // sort them to show job IDs only when needed
                    Collections.sort(cpus);
                    int job_span = se.getTick() - jobstart.getTick();

                    // print all rectangles corresponding to allocated CPUs
                    for (int p = 0; p < cpus.size(); p++) {
                        try {
                            if(draw_age || draw_user){
                              g2.setColor(Color.WHITE);  
                              g2.fillRect((xpos_start + (jobstart.getTick() * jobw)), (ypos + (jobh * (cpus.get(p) + jobstart.getCpu_shift()))), jobw * job_span, jobh);  
                              g2.setColor(new Color(r, g, b));                              
                              g2.fillRect((xpos_start + (jobstart.getTick() * jobw))+1, (ypos + (jobh * (cpus.get(p) + jobstart.getCpu_shift()))), (jobw * job_span)-1, jobh);  
                            }else{
                              g2.fillRect((xpos_start + (jobstart.getTick() * jobw)), (ypos + (jobh * (cpus.get(p) + jobstart.getCpu_shift()))), jobw * job_span, jobh);
                            }

                        } catch (Exception e) {
                            System.out.println("-----------------------------------------------------------");
                            System.out.println("Error: " + e.getMessage());
                            System.out.println("-------------------------stack trace-----------------------");
                            e.printStackTrace();
                        }
                    }
                    // print job IDs over previously drawn rectangles (i.e., jobs)
                    int prev_cpu_index = -2;
                    for (int p = 0; p < cpus.size(); p++) {
                        try {
                            if (cpus.get(p) != (prev_cpu_index + 1)) {
                                if (r + g + b <= 255) {
                                    g2.setColor(new Color(212, 212, 212));
                                } else {
                                    g2.setColor(Color.BLACK);
                                }
                                Font titleFont = new Font("Sans-Serif", Font.PLAIN, 9);
                                g2.setFont(titleFont);
                                g2.drawString(se.getGi().getID() + "", (xpos_start + (jobstart.getTick() * jobw)), ((ypos + (jobh * (cpus.get(p) + jobstart.getCpu_shift()))) + jobh) + 2);
                                g2.setColor(Color.WHITE);
                                g2.drawLine((xpos_start + (jobstart.getTick() * jobw)), (ypos + (jobh * (cpus.get(p) + jobstart.getCpu_shift()))), (xpos_start + (jobstart.getTick() * jobw))+(jobw * job_span), (ypos + (jobh * (cpus.get(p) + jobstart.getCpu_shift()))));
                            }

                        } catch (Exception e) {
                            System.out.println("-----------------------------------------------------------");
                            System.out.println("Error string: " + e.getMessage());
                            System.out.println("-------------------------stack trace-----------------------");
                            e.printStackTrace();
                        }
                        prev_cpu_index = cpus.get(p);
                    }

                    // print the start time stamp on x-axis
                    Graphics2D g22 = (Graphics2D) graphics;
                    g22.rotate(Math.toRadians(270), (xpos_start + (jobstart.getTick() * jobw) + (jobw / 2)), schedule_height + 90);
                    String start_date = new java.text.SimpleDateFormat("kk:mm:ss dd-MM-yyyy").format(new java.util.Date(jobstart.getSch_time() * 1000));
                    g2.setColor(Color.BLACK);
                    g22.drawString(start_date, (xpos_start + (jobstart.getTick() * jobw) + (jobw / 2)), schedule_height + 90);
                    g22.rotate(-Math.toRadians(270), (xpos_start + (jobstart.getTick() * jobw) + (jobw / 2)), schedule_height + 90);

                    //and print the end time too on the x-axis
                    g22 = (Graphics2D) graphics;
                    g22.rotate(Math.toRadians(270), (xpos_start + (se.getTick() * jobw) + (jobw / 2)), schedule_height + 90);
                    String end_date = new java.text.SimpleDateFormat("kk:mm:ss dd-MM-yyyy").format(new java.util.Date(se.getSch_time() * 1000));
                    g2.setColor(Color.BLACK);
                    g22.drawString(end_date, (xpos_start + (se.getTick() * jobw) + (jobw / 2)), schedule_height + 90);
                    g22.rotate(-Math.toRadians(270), (xpos_start + (se.getTick() * jobw) + (jobw / 2)), schedule_height + 90);
                }
            }

            // draw scale
            if (draw_age) {
                int scale = 1;
                if (256 * jobh > schedule_height) {
                    scale = Long.valueOf(Math.round((Math.ceil((256.0 * jobh) / schedule_height)))).intValue();
                }
                double distance = max_wait - min_wait;
                double step = Math.round(distance / 256);
                for (int pos = 0; pos < 256; pos++) {
                    double curr = min_wait + (pos * step);
                    double position = curr - min_wait;
                    position = position / distance;
                    int rgb_position = Long.valueOf(Math.round(position * 255)).intValue();
                    int r = 255;
                    int g = Math.min(255, 255 - rgb_position);
                    g = Math.max(0, g);
                    int b = 0;                    
                    if (pos % scale == 0) {
                        g2.setColor(Color.WHITE);
                        g2.fillRect(w - 105, (ypos + (jobh * (pos / scale))), 100, jobh);
                        g2.setColor(new Color(r, g, b));
                        g2.fillRect(w - 100, (ypos + (jobh * (pos / scale))), jobw, jobh);
                        if (pos % (3 * scale) == 0) {
                            String time = "";
                            if (curr >= 3600) {
                                time = Math.round((curr * 10.0) / 3600.0) / 10.0 + " hrs.";
                            }
                            if (curr < 3600) {
                                time = Math.round((curr * 10.0) / 60.0) / 10.0 + " min.";
                            }
                            if (curr < 60) {
                                time = Math.round(curr * 10) / 10.0 + " sec.";
                            }
                            Font titleFont = new Font("Sans-Serif", Font.PLAIN, 9);
                            g2.setFont(titleFont);
                            g2.drawString(time, w - 80, (ypos + (jobh * (pos / scale)) + jobh));
                        }
                    }
                }

            }

            // draw graph title
            g2.setColor(Color.BLACK);
            Font titleFont = new Font("Sans-Serif", Font.PLAIN, 9);
            g2.setFont(titleFont);
            g2.drawString("schedule overview (time not to scale)", w / 2 - 70, 7);
            job_schedule.clear();
            this.draw_schedule = true;
        }
    }

    /**
     * run thread
     */
    public void run() {
        repaint();
    }

    /**
     * redraw certain graph based on input data
     */
    public void reDrawSchedule(ArrayList[] schedules, int cl_count, LinkedList cl_names, LinkedList cl_CPUs) {
        this.draw_schedule = true;
        this.cl_names = cl_names;
        this.cl_CPUs = cl_CPUs;
        this.cl_count = cl_count;
        this.schedules = schedules;
        repaint();
    }

    /**
     * start thread.
     */
    public void start() {
        if (!animating) {
            animating = true;
            thread = new Thread(this);
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.start();
        }
    }

    /**
     * stop thread.
     */
    public void stop() {
        animating = false;
        if (thread != null) {
            thread.interrupt();
        }
        thread = null;
    }

    /**
     * generates bitmap image.
     */
    private BufferedImage makeImage() {
        int w = getWidth();
        int h = getHeight();
        int type = BufferedImage.TYPE_INT_RGB;
        BufferedImage imagew = new BufferedImage(w, h, type);
        Graphics2D g2 = imagew.createGraphics();
        paint(g2);
        g2.dispose();
        return imagew;
    }

    /**
     * saves bitmap image into file.
     */
    private void save(BufferedImage image, String i, String ext, String identifier) {
        System.out.println("saving file to: " + ext);
        //File file = new File(System.getProperty("user.dir") + File.separator + ExperimentSetup.getDir(DirectoryLevel.EXPERIMENT_ROOT) + File.separator + "graphs/" + identifier + "-" + i + "." + ext);
        File file = new File(ExperimentSetup.getDirG(DirectoryLevel.GRAPHSALG) + File.separator + identifier + "-" + i + "." + ext);
        try {
            ImageIO.write(image, ext, file);
        } catch (IOException e) {
            System.out.println("write error: " + e.getMessage());
        }
    }

    /**
     * method called by the MainFrame instance. This method will call save
     * method inside this class.
     */
    public void saveToFile3(String ext, String identifier) {
        save(makeImage(), getName(), ext, identifier);
    }

    public static void saveImages() {
        Date d = new Date();
        String identifier = "" + d.getTime();
        for (Schedule_Visualizator iaw : ia) {
            iaw.saveToFile3("png", identifier);
        }
    }

    /**
     * This method initializes the GUI, creating all windows that will be used
     * to draw results.
     */
    public static void createGUI(LinkedList<Schedule_Visualizator> windows) {

        ia = new LinkedList();

        Schedule_Visualizator sched_vis = new Schedule_Visualizator();
        JFrame frame = new JFrame();
        frame.setTitle("Schedule overview (time not to scale)");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        //test.setOpaque(false);
        frame.getContentPane().add(sched_vis);
        sched_vis.setBackground(Color.white);
        frame.setSize(1650, 1100);
        frame.setLocation(150, 0);
        frame.setVisible(true);
        sched_vis.setName("schedule");
        sched_vis.start();
        windows.add(sched_vis);

        ia.add(sched_vis);

        MainFrame mf = new MainFrame(ia, true);
        mf.setVisible(true);
    }

    public List<Integer> getListCopy(List<Integer> oldList) {
        synchronized (oldList) {
            return new ArrayList<>(oldList);
        }

    }
}
