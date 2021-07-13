pipeline {
	agent {
		label 'migration'
	}
  
	tools {
		maven 'apache-maven-latest'
		jdk 'openjdk-jdk16-latest'
	}
  
	environment {
		BUILD_KEY = (github.isPullRequest() ? CHANGE_TARGET : BRANCH_NAME).replaceFirst(/^v/, '')
		CAPELLA_PRODUCT_PATH = "${WORKSPACE}/releng/plugins/org.polarsys.capella.rcp.product/target/products/org.polarsys.capella.rcp.product/linux/gtk/x86_64/capella"
  	}
  
  	stages {
  	
		stage('Generate Target Platform') {
	    	steps {        
	        	script { 
		        	if(github.isPullRequest()){
		        	    github.buildStartedComment()
		        	    
		        	    github.removeCustomPullRequestLabels()
		            	github.buildStartedLabel()
		        	}
		
		        	currentBuild.description = BUILD_KEY
		        	
		        	sh 'env'
		        	sh 'mvn clean verify -f releng/plugins/org.polarsys.capella.targets/pom.xml'
	       		}
	     	}
	    }
	    
	    stage('Download JDK') {
	    	steps {        
	        	script { 
		            def jdkWinFolder = 'winJRE'
		            downloader.downloadWindowsJDK16(jdkWinFolder)
		            
		            def jdkLinuxFolder = 'linuxJRE'
		            downloader.downloadLinuxJDK16(jdkLinuxFolder)
		            
		            def jdkMacFolder = 'macJRE'
		            downloader.downloadMacJDK16(jdkMacFolder)
	       		}
	     	}
	    }
	    
    	stage('Build and Package') {
      		steps {
      			script {
					withCredentials([string(credentialsId: 'sonar-token-capella', variable: 'SONARCLOUD_TOKEN')]) {
						withEnv(['MAVEN_OPTS=-Xmx6g']) {
							def sign = github.isPullRequest() ? '' : '-Psign'
							sh "mvn clean verify -f pom.xml -DjavaDocPhase=none -Pfull ${sign}"
						}
					}
      			}
	     	}
	    }
	    
		stage('Deploy to Nightly') {
      		steps {
				script {		
		    		def deploymentDirName = 
		    			(github.isPullRequest() ? "${BUILD_KEY}-${BRANCH_NAME}-${BUILD_ID}" : "${BRANCH_NAME}-${BUILD_ID}")
		    			.replaceAll('/','-')		
		
				    deployer.capellaNightlyProduct("${WORKSPACE}/releng/plugins/org.polarsys.capella.rcp.product/target/products/capella-*.zip", deploymentDirName)
				    deployer.capellaNightlyProduct("${WORKSPACE}/releng/plugins/org.polarsys.capella.rcp.product/target/products/capella-*.gz", deploymentDirName)
				    
				    deployer.capellaNightlyUpdateSite("${WORKSPACE}/releng/plugins/org.polarsys.capella.test.site/target/repository/**", "${deploymentDirName}/org.polarsys.capella.test.site")
				    deployer.capellaNightlyUpdateSite("${WORKSPACE}/releng/plugins/org.polarsys.capella.egf.site/target/repository/**", "${deploymentDirName}/org.polarsys.capella.egf.site")
				    deployer.capellaNightlyUpdateSite("${WORKSPACE}/releng/plugins/org.polarsys.capella.rcp.site/target/repository/**", "${deploymentDirName}/org.polarsys.capella.rcp.site")
				    deployer.capellaNightlyUpdateSite("${WORKSPACE}/releng/plugins/org.polarsys.capella.targets/full/*", "${deploymentDirName}/targets")
				    
				    currentBuild.description = "${deploymentDirName} - <a href=\"https://ci-staging.eclipse.org/capella/view/Capella/job/capella-product/\">build</a> - <a href=\"https://download.eclipse.org/capella/core/products/nightly/${deploymentDirName}\">product</a>" 				
	       		}         
	     	}
	    }
    
    	stage('Deploy to Nightly Root') {
      		when {
        		expression { 
        			!github.isPullRequest() 
        		}
      		}
      		
      		steps {
      			script {
			        def deploymentDirName = BUILD_KEY		
			
					deployer.cleanCapellaNightlyArtefacts(deploymentDirName)
			
					deployer.capellaNightlyProduct("${WORKSPACE}/releng/plugins/org.polarsys.capella.rcp.product/target/products/capella-*.zip", deploymentDirName)
					deployer.capellaNightlyProduct("${WORKSPACE}/releng/plugins/org.polarsys.capella.rcp.product/target/products/capella-*.gz", deploymentDirName)
					    
					deployer.capellaNightlyUpdateSite("${WORKSPACE}/releng/plugins/org.polarsys.capella.test.site/target/repository/**", "${deploymentDirName}/org.polarsys.capella.test.site")
					deployer.capellaNightlyUpdateSite("${WORKSPACE}/releng/plugins/org.polarsys.capella.egf.site/target/repository/**", "${deploymentDirName}/org.polarsys.capella.egf.site")
					deployer.capellaNightlyUpdateSite("${WORKSPACE}/releng/plugins/org.polarsys.capella.rcp.site/target/repository/**", "${deploymentDirName}/org.polarsys.capella.rcp.site")
					deployer.capellaNightlyUpdateSite("${WORKSPACE}/releng/plugins/org.polarsys.capella.targets/full/*", "${deploymentDirName}/targets")
			      
			      	currentBuild.description = "${BUILD_KEY} - <a href=\"https://ci-staging.eclipse.org/capella/view/Capella/job/capella-product/\">build</a> - <a href=\"https://download.eclipse.org/capella/core/products/nightly/${env.BUILD_KEY}\">product</a>" } 
			}
		}

    	stage('Install test features') {
    		
        	steps {
        		script {
	        		sh "chmod 755 ${CAPELLA_PRODUCT_PATH}"
	        			        		       		
	        		eclipse.installFeature("${CAPELLA_PRODUCT_PATH}", "file:/${WORKSPACE}/releng/plugins/org.polarsys.capella.test.site/target/repository".replace("\\", "/"), 'org.polarsys.capella.test.feature.feature.group')
	       		}         
	     	}
	    }
	    
    	stage('Run tests') {
    		
        	steps {
        		script {
        			wrap([$class: 'Xvnc', takeScreenshot: false, useXauthority: true]) {
		        		
		        		tester.runUITests("${CAPELLA_PRODUCT_PATH}", 'ModelQueriesValidation', 'org.polarsys.capella.test.suites.ju', 
		        			['org.polarsys.capella.test.business.queries.ju.testSuites.main.BusinessQueryTestSuite',
		        			 'org.polarsys.capella.test.semantic.queries.ju.testsuites.SemanticQueriesTestSuite', 
		        			 'org.polarsys.capella.test.validation.rules.ju.testsuites.main.ValidationRulesTestSuite'])
		        			 
		        		tester.runUITests("${CAPELLA_PRODUCT_PATH}", 'LibRecTransition', 'org.polarsys.capella.test.suites.ju', 
		        			['org.polarsys.capella.test.libraries.ju.testsuites.main.LibrariesTestSuite',
		        			  'org.polarsys.capella.test.recrpl.ju.testsuites.main.RecRplTestSuite',
		        			  'org.polarsys.capella.test.transition.ju.testsuites.main.TransitionTestSuite',
		        			  'org.polarsys.capella.test.re.updateconnections.ju.UpdateConnectionsTestSuite'])
		        		
		        		tester.runUITests("${CAPELLA_PRODUCT_PATH}", 'DiagramTools1', 'org.polarsys.capella.test.suites.ju', 
		        			['org.polarsys.capella.test.diagram.tools.ju.testsuites.main.DiagramToolsStep1TestSuite'])
		        			
		        		tester.runUITests("${CAPELLA_PRODUCT_PATH}", 'DiagramTools2', 'org.polarsys.capella.test.suites.ju', 
		        			['org.polarsys.capella.test.diagram.tools.ju.testsuites.main.DiagramToolsStep2TestSuite'])
		        			        			
		        		tester.runUITests("${CAPELLA_PRODUCT_PATH}", 'DiagramMiscFilters', 'org.polarsys.capella.test.suites.ju', 
		        			['org.polarsys.capella.test.diagram.misc.ju.testsuites.DiagramMiscTestSuite',
		        			  'org.polarsys.capella.test.diagram.filters.ju.testsuites.DiagramFiltersTestSuite',
		        			  'org.polarsys.capella.test.table.ju.testsuite.TableTestSuite'])
		        			    
		        		tester.runUITests("${CAPELLA_PRODUCT_PATH}", 'Fragmentation', 'org.polarsys.capella.test.suites.ju', 
		        			['org.polarsys.capella.test.fragmentation.ju.testsuites.FragmentationTestSuite'])
							
						tester.runUITests("${CAPELLA_PRODUCT_PATH}", 'Odesign', 'org.polarsys.capella.test.suites.ju', 
		        			['org.polarsys.capella.test.odesign.ju.maintestsuite.ODesignTestSuite'])
		   
		        		tester.runUITests("${CAPELLA_PRODUCT_PATH}", 'Views', 'org.polarsys.capella.test.suites.ju', 
		        			['org.polarsys.capella.test.model.ju.testsuites.main.ModelTestSuite', 
		        			 'org.polarsys.capella.test.massactions.ju.testsuites.MassActionsTestSuite',
		        			 'org.polarsys.capella.test.platform.ju.testsuites.PlatformTestSuite', 
		        			 'org.polarsys.capella.test.richtext.ju.testsuites.RichtextTestSuite',
		        			 'org.polarsys.capella.test.fastlinker.ju.testsuites.FastLinkerTestsSuite',
		        			 'org.polarsys.capella.test.explorer.activity.ju.testsuites.ActivityExplorerTestsSuite',
		        			 'org.polarsys.capella.test.progressmonitoring.ju.testsuites.SetProgressTestSuite',
		        			 'org.polarsys.capella.test.navigator.ju.testsuites.main.NavigatorUITestSuite'])
		        			 
		        		tester.runUITests("${CAPELLA_PRODUCT_PATH}", 'MigrationCommandLine', 'org.polarsys.capella.test.suites.ju', 
		        			['org.polarsys.capella.test.migration.ju.testsuites.main.MigrationTestSuite',
		        			 'org.polarsys.capella.test.diagram.layout.ju.testsuites.LayoutTestSuite',
		        			 'org.polarsys.capella.test.commandline.ju.testsuites.CommandLineTestSuite'])
		 	
		        		tester.runUITests("${CAPELLA_PRODUCT_PATH}", 'Benchmark', 'org.polarsys.capella.test.suites.ju', 
		        			['org.polarsys.capella.test.benchmarks.ju.suites.AllBenchmarksTestSuite'])
		   
		  				tester.runUITests("${CAPELLA_PRODUCT_PATH}", 'Detach', 'org.polarsys.capella.test.suites.ju', 
		        			['org.polarsys.capella.test.model.ju.testsuites.partial.DetachTestSuite'])
		        			
		        		tester.runUITests("${CAPELLA_PRODUCT_PATH}", 'Documentation', 'org.polarsys.capella.test.doc.ju', 
		        			['org.polarsys.capella.test.doc.ju.testsuites.DocTestSuite'])
		        			
		        		tester.runNONUITests("${CAPELLA_PRODUCT_PATH}", 'NotUINavigator', 'org.polarsys.capella.test.suites.ju', 
		        			['org.polarsys.capella.test.navigator.ju.testsuites.main.NavigatorTestSuite'])
		        			
	        		}
	        		
	        		tester.publishTests()
				}
			}
		}
		
		stage('Sonar') {
			steps {
				script {
					sonar.runSonar("eclipse_capella", "eclipse/capella", "sonar-token-capella")
				}
			}
		}
		
	}
  
	post {
    	always {
       		archiveArtifacts artifacts: '**/*.log, *.log, *.xml, **/*.layout, *.exec'
       		
       		script {
       		    github.removeBuildStartedLabel()
       		}
    	}
    	
    	success  {
    		script {
    			if(github.isPullRequest()){
        			github.buildSuccessfullComment()
        			github.buildSuccessfullLabel()
        			
        		}
        	}
    	}
    	
	    unstable {
	    	script {
	    		if(github.isPullRequest()){
	        		github.buildUnstableComment()
	        		github.buildUnstableLabel()
	        	}
	        }
	    }
    
	    failure {
	    	script {
	    		if(github.isPullRequest()){
	        		github.buildFailedComment()
	        		github.buildFailedLabel()
	        	}
	        }
	    }
	    
	    aborted {
	    	script {
	    		if(github.isPullRequest()){
	        		github.buildAbortedComment()
	        		github.buildAbortedLabel()
	        	}
	        }
	    }
	}
}