package it.unitn.disi.smatch.test;

import static org.junit.Assert.assertTrue;
import it.unitn.disi.smatch.IMatchManager;
import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to load several configs to check if anything explodes
 * Config loading is done with API calls.
 * <p>
 * <b>NOTE</b>: running dir during testing is {@code src/main/resources/bin}
 * </p>
 * 
 * @since 2.0.0
 * @author <a rel="author" href="http://davidleoni.it/">David Leoni</a>
 */
public class ConfigTest {

    private static final Logger log = LoggerFactory.getLogger(ConfigTest.class);

    /**
     * 
     * @since 2.0.0
     */
    private IContextMapping<INode> runPipeline(String configFilename) throws SMatchException {
        log.info("****  CONFIG: " + configFilename);
        log.info("Creating MatchManager...");

        IMatchManager mm = MatchManager.getInstanceFromConfigFile("../conf/" + configFilename);

        log.info("Creating source context...");
        IContext s = mm.createContext();
        INode sroot = s.createRoot("dog");
        sroot.createChild("cat");
        sroot.createChild("fish");

        IContext t = mm.createContext();
        INode troot = t.createRoot("dog");
        troot.createChild("feline");
        INode tfish = troot.createChild("fish");
        tfish.createChild("carp");

        IContextMapping<INode> result = mm.match(s, t);

        log.info("Processing results...");
        log.info("Printing matches:");
        for (IMappingElement<INode> e : result) {
            log.info(e.getSource()
                      .nodeData()
                      .getName() + "\t" + e.getRelation() + "\t" + e.getTarget()
                                                                    .nodeData()
                                                                    .getName());
        }

        result = mm.match(s, t);

        log.info("Processing results...");
        log.info("Printing matches:");
        for (IMappingElement<INode> e : result) {
            log.info(e.getSource()
                      .nodeData()
                      .getName() + "\t" + e.getRelation() + "\t" + e.getTarget()
                                                                    .nodeData()
                                                                    .getName());
        }

        log.info("Done");

        return result;
    }

    /**
     * Ignored because of Async.setProgress error in core:
     * https://github.com/s-match/s-match-core/issues/13
     * 
     * @since 2.0.0
     */
    @Test
    @Ignore
    public void testDefault() throws SMatchException {

        IContextMapping<INode> results = runPipeline("s-match.xml");

        assertTrue(results.size() > 0);

        // TODO need better check

    }

    /**
     * 
     * @since 2.0.0
     */
    @Test
    public void testSynchronous() throws SMatchException {

        IContextMapping<INode> results = runPipeline("s-match-synchronous.xml");

        assertTrue(results.size() > 0);

        // TODO need better check

    }

    /**
     * Test case for <a href="https://github.com/s-match/s-match-core/issues/10"
     * target="_blank"> #10 in core, async not working</a>
     * 
     * @throws SMatchException
     * 
     * @since 2.0.0
     */
    @Test
    public void testSpsm() throws SMatchException {

        IContextMapping<INode> results = runPipeline("s-match-spsm.xml");

        assertTrue(results.size() > 0);

        // TODO need better check

    }

    /**
     * Test case for <a href="https://github.com/s-match/s-match-core/issues/10"
     * target="_blank"> #10 in core, async not working</a>
     * and also <a href="https://github.com/s-match/s-match-utils/issues/4"
     * target="_blank"> #4 in utils, asymm not working</a>
     * 
     * @throws SMatchException
     * 
     * @since 2.0.0
     */
    @Test
    public void testSpsmAsymmetric() throws SMatchException {

        IContextMapping<INode> results = runPipeline("s-match-spsm-asymmetric.xml");

        assertTrue(results.size() > 0);

        // TODO need better check
    }

    /**
     * 
     * @since 2.0.0
     */
    @Test
    public void testSpsmFunction() throws SMatchException {

        IContextMapping<INode> results = runPipeline("s-match-spsm-function.xml");

        assertTrue(results.size() > 0);

        // TODO need better check

    }

}
