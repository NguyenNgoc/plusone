import sys

import math
from math import e

import random
from random import random as rand
from random import shuffle

import numpy
from numpy.random.mtrand import dirichlet

class Poisson(object):
    def __init__(self, L=15):
        self.lamb = L
        
    def sample(self):
        L = e ** (-self.lamb)
        k, p = 1, rand()
        while p > L:
            k += 1
            p *= rand()
        return k - 1

def sample(dist):
    """takes a list of probabilities and samples from it by index
       assumes a multinomial distribution
    Documentation TODO
    """
    p = rand()
    res = 0
    for i in range(len(dist)):
        res += dist[i]
        if res > p:
            return i
    #for debugging purposes only *SHOULD NOT REACH THIS LINE*
    print res, p

def count(words):
    word_count = {}
    num_words = 0
    for word in words:
        num_words += 1
        if word_count.has_key(word):
            word_count[word] += 1
        else:
            word_count[word] = 1
    word_count["total"] = num_words
    return word_count

def generate_docs(num_topics, num_docs, words_per_doc=50, vocab_size=30,
                  alpha=None, beta=None):
    p = Poisson(words_per_doc)
    if alpha == None:
        alpha = [1]*num_topics
    if beta == None:
        beta = [1]*vocab_size
    if len(alpha) != num_topics or len(beta) != vocab_size:
        print "ERROR: dirichlet parameters unequal:"
        print "alpha supplied:", len(alpha), "(needed", num_topics, ")"
        print "beta supplied:", len(beta), "(needed", vocab_size, ")" 
        return
    word_dist = [dirichlet(beta) for i in range(num_topics)]
    docs = []
    topic_dists = []
    for i in range(num_docs):
        words_per_doc = p.sample()
        doc = []
        topic_dist = dirichlet(alpha)
        topic_dists.append(topic_dist)
        for word in range(words_per_doc):
            topic = sample(topic_dist)
            doc.append(sample(word_dist[topic]))
        docs.append(doc)
    return docs, word_dist, topic_dists

def write(docs):
    with open('lda-out', 'w') as f:
        for doc in docs:
            for word in doc:
                f.write(str(word) + " ")
            f.write('\n')

def main():
    args = sys.argv[1:]
    if len(args) == 2:
        num_topics, num_docs = [int(arg) for arg in args]
    else:
        num_topics, num_docs = 4, 20
        print "using default parameters for num_docs and num_topics"
    
    print "generating", num_docs, "documents with", num_topics, "topics"
    print ""
    data = generate_docs(num_topics, num_docs)
    if '-w' in args:
        print "writing data to file...",
        write(data[0])
        print "done"
    return data

if __name__ == '__main__':
    docs, words, topics = main()
