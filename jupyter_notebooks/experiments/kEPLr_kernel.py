from time import sleep

import requests
from ipykernel.kernelbase import Kernel



class EsperConnection:

    pathname=""
    host = "http://localhost:4567"


    def __init__(self, output, username):
        self.username = username
        self.output = output
        self.session = requests.session()
        #self.headers = {'user_id': username, 'output' : 'true'}


    def process_magics(self, line):


        # Split line into command & parameters

        cmd = line

        cmd = cmd[1:].lower()

        # Process each magic
        if cmd == 'output':
            self.pathname = "/output"
            self.host = "http://outputmanager:1234"
        elif cmd == 'input':
            self.pathname = "/input"
            self.host = "http://inputmanager:4567"

    def sendMessage(self, content):

        r = self.session.post(self.host+self.pathname, content)
        if self.pathname=="/output":
            r = self.session.request('GET', self.host+'/output')
            print(r.text)
        return r.text

    def switchDest(self, nwDest):
        self.pathname = nwDest

    def switchHost(self, nwHost):
        self.host = nwHost


class kEPLrKernel(Kernel):
    implementation = 'kEPLr'
    implementation_version = '1.0'
    language = 'python'  # will be used for
    # syntax highlighting
    language_version = '3.6'
    language_info = {'name': 'kEPLr',
                     'mimetype': 'text/plain',
                     'extension': '.py'}
    banner = "Experimental environment for EPL"
    connection = EsperConnection("true", 'sem')

    def do_execute(self, code, silent,
                   store_history=True,
                   user_expressions=None,
                   allow_stdin=False):

        functions = code.split('\n')

        line = functions[0]
        n=0

        # Processing the magics first, setting up the destination
        if line[0] == '%':
            self.connection.process_magics(line)
            n = 1
        else:
            self.connection.switchDest("/query")
            self.connection.switchHost("http://runtime:7890")
        res = self.connection.sendMessage('\n'.join(functions[n:]))


        if not silent:
            # We send the standard output to the
            # client.
            self.send_response(
                self.iopub_socket,
                'stream', {
                    'name': 'stdout',
                    'data': ('Processing the request.')})

            # We prepare the response, setting up the format.
            content = {
                'source': 'kernel',
                'data': {
                    'text/plain': res
                },

            }

            # We send the display_data message with
            # the contents.
            self.send_response(self.iopub_socket,
                               'display_data', content)

        # We return the execution results.
        return {'status': 'ok',
                'execution_count':
                    self.execution_count,
                'payload': [],
                'user_expressions': {},
                }

if __name__ == '__main__':
    from ipykernel.kernelapp import IPKernelApp
    IPKernelApp.launch_instance(
        kernel_class=kEPLrKernel)







