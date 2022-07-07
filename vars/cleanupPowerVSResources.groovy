def call()
{
    script {
        if ( env.POWERVS == "true" ) {
            def logContent = Jenkins.getInstance().getItemByFullName(env.JOB_NAME).getBuildByNumber(Integer.parseInt(env.BUILD_NUMBER)).logFile.text
            if (env.VM_ID_PREFIX) {
                env.CLUSTER_ID_PREFIX="${VM_ID_PREFIX}"
            }
            else {
                env.CLUSTER_ID_PREFIX="${INSTANCE_NAME}"
            }
            env.CLUSTER_ID = logContent.find("${CLUSTER_ID_PREFIX}-[A-Za-z0-9]{4}")
            if (env.CRN == null) {
                env.CRN="crn:v1:bluemix:public:power-iaas:tor01:a/7cfbd5381a434af7a09289e795840d4e:007e0e92-91d5-4f30-bc63-ca515660a4c2::"
            }
           if (env.CLUSTER_ID != null) {
                sh '''
                    chmod +x ./scripts/cleanup-powervs-resources.sh
                    ./scripts/cleanup-powervs-resources.sh -u "${CLUSTER_ID}" -s ""
                '''
           }
        }
    }
}
