import pickle

def read_config(path):
    with open(path, 'rb') as config_file:
        _dict = pickle.loads(config_file.read())
        return _dict


def write_config(path, config):
    with open(path, 'wb') as config_file:
        pickle.dump(config, config_file)


def get_default_config():
