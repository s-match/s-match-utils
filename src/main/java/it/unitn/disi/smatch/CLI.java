package it.unitn.disi.smatch;

import it.unitn.disi.common.DISIException;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.trees.IBaseContext;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.loaders.context.IContextLoader;
import it.unitn.disi.smatch.oracles.wordnet.InMemoryWordNetBinaryArray;
import it.unitn.disi.smatch.oracles.wordnet.WordNet;
import it.unitn.disi.smatch.renderers.context.IContextRenderer;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Command-line interface for S-Match.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class CLI {

    private static final Logger log = LoggerFactory.getLogger(CLI.class);

    static {
        String log4jConf = System.getProperty("log4j.configuration");
        if (null != log4jConf) {
            PropertyConfigurator.configure(log4jConf);
        }
    }

    /**
     * Default configuration file name.
     */
    public static final String DEFAULT_CONFIG_FILE_NAME = "/it/unitn/disi/smatch/s-match.xml";

    // config file command line key
    public static final String CONFIG_FILE_CMD_LINE_KEY = "-config=";

    /**
     * Create cached WordNet files for fast matching:
     * 
     * <pre> 
     * {@code wntoflat <jwnlConfig> <files...>}
     * </pre>
     */
    public static final String CMD_WN_TO_FLAT = "wntoflat";    

    /**
     * Read input file and write it into output file:
     * 
     * <pre>
     * {@code convert <input> <output>}
     * </pre>
     * 
     * Read source, target and input mapping, and write the output mapping:
     * 
     * <pre>
     * {@code convert <source> <target> <input> <output>}
     * </pre>
     */
    public static final String CMD_CONVERT = "convert";    
               
    /**
     * Read input file, preprocess it and write it into output file:
     * 
     * <pre>
     * {@code offline <input> <output> }
     * </pre>    
     */
    public static final String CMD_OFFLINE = "offline";
                 
    /** 
     * Read source and target files, run matching and write the output file:
     * 
     * <pre>
     * {@code online <source> <target> <output> }
     * </pre>    
     */
    public static final String CMD_ONLINE = "online";
           
    /**
     * Read source and target files, input mapping, run filtering and write the output mapping:
     * 
     * <pre>
     * {@code filter <source> <target> <input> <output>}
     * </pre>    
     */
    public static final String CMD_FILTER = "filter";
    
    
    /** 
     * Read source and target files, run all steps from 1 to 4 and write the output mapping:
     * 
     * <pre>
     * {@code  allsteps <source> <target> <output>  }
     * </pre>
     * 
     * @since 2.0.0
     */
    public static final String CMD_ALL_STEPS = "allsteps";
    
    // usage string
    private static final String USAGE = "Usage: MatchManager <command> <arguments> [options]\n" +
            " Commands: \n" +
            " wntoflat <jwnlConfig> <files...>           create cached WordNet files for fast matching\n" +
            " convert <input> <output>                   read input file and write it into output file\n" +
            " convert <source> <target> <input> <output> read source, target and input mapping, and write the output mapping\n" +
            " offline <input> <output>                   read input file, preprocess it and write it into output file\n" +
            " online <source> <target> <output>          read source and target files, run matching and write the output file\n" +
            " filter <source> <target> <input> <output>  read source and target files, input mapping, run filtering and write the output mapping\n" +
            " allsteps <source> <target> <output>        read source and target files, run all steps from 1 to 4 and write the output mapping\n" +            
            "\n" +
            " Options: \n" +
            " -config=file.xml                           read configuration from file.xml instead of default s-match.xml\n" +
            "                                            use -Dkey=value to supply values to ${key} placeholders in the config file\n";    

    /**
     * Provides command line interface to the match manager.
     *
     * @param args command line arguments
     * @throws IOException            IOException
     * @throws DISIException          DISIException
     * @throws ClassNotFoundException ClassNotFoundException
     */
    public static void main(String[] args) throws IOException, DISIException, ClassNotFoundException {
        // initialize property file
        String configFileName = null;
        
        ArrayList<String> cleanArgs = new ArrayList<>();
        for (String arg : args) {
            if (arg.startsWith(CONFIG_FILE_CMD_LINE_KEY)) {
                configFileName = arg.substring(CONFIG_FILE_CMD_LINE_KEY.length());
                System.out.println("Using config file: " + configFileName);
            } else {
                cleanArgs.add(arg);
            }
        }

        args = cleanArgs.toArray(new String[cleanArgs.size()]);

        // check input parameters
        if (args.length < 1) {
            log.info(USAGE);
        } else {
            IMatchManager mm;

            switch (args[0]) {
                case CMD_WN_TO_FLAT:
                    if (9 < args.length) {
                        CLI.convertWordNetToFlat(
                                args[1],
                                args[2],
                                args[3],
                                args[4],
                                args[5],
                                args[6],
                                args[7],
                                args[8],
                                args[9]
                        );
                    } else {
                        log.error("Not enough arguments for wntoflat command.");
                    }
                    break;
                case CMD_CONVERT:
                    mm = createMatchManager(configFileName);
                    if (2 < args.length) {
                        if (3 == args.length) {
                            String inputFile = args[1];
                            String outputFile = args[2];
                            IBaseContext ctxSource = mm.loadContext(inputFile);
                            mm.renderContext(ctxSource, outputFile);
                        } else if (5 == args.length) {
                            String sourceFile = args[1];
                            String targetFile = args[2];
                            String inputFile = args[3];
                            String outputFile = args[4];

                            if (mm.getContextLoader() instanceof IContextLoader) {
                                IContext ctxSource = (IContext) mm.loadContext(sourceFile);
                                IContext ctxTarget = (IContext) mm.loadContext(targetFile);
                                IContextMapping<INode> map = mm.loadMapping(ctxSource, ctxTarget, inputFile);
                                mm.renderMapping(map, outputFile);
                            } else {
                                log.warn("To convert a mapping, use context loaders supporting IContextLoader.");
                            }
                        }
                    } else {
                        log.error("Not enough arguments for convert command.");
                    }
                    break;
                case CMD_OFFLINE:
                    mm = createMatchManager(configFileName);
                    if (2 < args.length) {
                        String inputFile = args[1];
                        String outputFile = args[2];
                        if (mm.getContextLoader() instanceof IContextLoader && mm.getContextRenderer() instanceof IContextRenderer) {
                            IContext ctxSource = (IContext) mm.loadContext(inputFile);
                            mm.offline(ctxSource);
                            mm.renderContext(ctxSource, outputFile);
                        } else {
                            log.warn("To preprocess a mapping, use context loaders and renderers support IContextLoader and IContextRenderer.");
                        }
                    } else {
                        log.error("Not enough arguments for offline command.");
                    }
                    break;
                case CMD_ONLINE:
                    mm = createMatchManager(configFileName);
                    if (3 < args.length) {
                        String sourceFile = args[1];
                        String targetFile = args[2];
                        String outputFile = args[3];
                        if (mm.getContextLoader() instanceof IContextLoader) {
                            IContext ctxSource = (IContext) mm.loadContext(sourceFile);
                            IContext ctxTarget = (IContext) mm.loadContext(targetFile);
                            IContextMapping<INode> result = mm.online(ctxSource, ctxTarget);
                            mm.renderMapping(result, outputFile);
                        } else {
                            log.warn("To match contexts, use context loaders supporting IContextLoader.");
                        }
                    } else {
                        log.error("Not enough arguments for online command.");
                    }
                    break;
                case CMD_FILTER:
                    mm = createMatchManager(configFileName);
                    if (4 < args.length) {
                        String sourceFile = args[1];
                        String targetFile = args[2];
                        String inputFile = args[3];
                        String outputFile = args[4];

                        if (mm.getContextLoader() instanceof IContextLoader) {
                            IContext ctxSource = (IContext) mm.loadContext(sourceFile);
                            IContext ctxTarget = (IContext) mm.loadContext(targetFile);
                            IContextMapping<INode> mapInput = mm.loadMapping(ctxSource, ctxTarget, inputFile);
                            IContextMapping<INode> mapOutput = mm.filterMapping(mapInput);
                            mm.renderMapping(mapOutput, outputFile);
                        } else {
                            log.warn("To filter a mapping, use context loaders supporting IContextLoader.");
                        }
                    } else {
                        log.error("Not enough arguments for filter command.");
                    }
                    break;
                case CMD_ALL_STEPS:
                    mm = createMatchManager(configFileName);
                    if (3 < args.length) {
                        String inputFile1 = args[1];
                        String inputFile2 = args[2];
                        String outputFile = args[3];
                        
                        if (mm.getContextLoader() instanceof IContextLoader) {
                            IContext ctxSource1 = (IContext) mm.loadContext(inputFile1);
                            mm.offline(ctxSource1);
                            IContext ctxSource2 = (IContext) mm.loadContext(inputFile2);
                            mm.offline(ctxSource2);
                            IContextMapping<INode> result = mm.online(ctxSource1, ctxSource2);
                            try {
                                IContextMapping<INode> mapOutput = mm.filterMapping(result);
                            } catch (SMatchException ex){
                                log.info("No filtering was performed (too see why, set logging at DEBUG level)");                                                               
                                log.debug("Reason:\n", ex);                                
                            }
                            mm.renderMapping(result, outputFile);
                        } else {
                            log.error("To preprocess a mapping, use context loaders that support IContextLoader");
                        }
                    } else {
                        log.error("Not enough arguments for allsteps command.");
                    }
                    break;
                default:
                    log.error("Unrecognized command.");
                    break;
            }
        }
    }

    public static IMatchManager createMatchManager(String configFileName) {
        IMatchManager mm;
        if (configFileName == null) {
            mm = MatchManager.getInstanceFromResource(DEFAULT_CONFIG_FILE_NAME);
            log.info("Using resource config file: " + DEFAULT_CONFIG_FILE_NAME);
        } else {
            mm = MatchManager.getInstanceFromConfigFile(configFileName);
        }
        return mm;
    }

    /**
     * Converts WordNet dictionary to binary format for fast searching.
     *
     * @throws SMatchException SMatchException
     */
    private static void convertWordNetToFlat(
            String jwnlPropertiesPath,
            String adjectiveSynonyms,
            String adjectiveAntonyms,
            String nounHypernyms,
            String nounAntonyms,
            String adverbAntonyms,
            String verbHypernyms,
            String nominalizations,
            String multiwords
    ) throws SMatchException {
        InMemoryWordNetBinaryArray.createWordNetCaches(
                jwnlPropertiesPath, adjectiveSynonyms, adjectiveAntonyms,
                nounHypernyms, nounAntonyms, adverbAntonyms, verbHypernyms, nominalizations);
        WordNet.createWordNetCaches(jwnlPropertiesPath, multiwords);
    }

}