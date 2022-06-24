# syntax=docker/dockerfile:1

from jupyter/base-notebook:latest

WORKDIR $HOME

COPY ./kEPLr_kernel /usr/local/share/jupyter/kernels/kEPLr_kernel
ENV JUPYTER_PATH=$HOME
COPY ./jupyter_notebooks ./jupyter_notebooks
USER root
RUN chown -R jovyan ./jupyter_notebooks
USER jovyan

WORKDIR ./jupyter_notebooks

