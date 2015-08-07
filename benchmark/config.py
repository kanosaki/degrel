

def artifact_url(self, project_id, build_num, path):
    "https://ci.teor.in/repository/downloadAll/{}/{}/{}" \
        % (project_id, build_num, path)



def artifact_zip(self, project_id, build_num):
    return artifact_url(project_id, build_num, 'artifacts.zip')

